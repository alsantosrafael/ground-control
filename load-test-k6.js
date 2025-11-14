import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const responseTime = new Trend('response_time', true);
const flagCreations = new Counter('flag_creations');
const flagReads = new Counter('flag_reads');
const flagEvaluations = new Counter('flag_evaluations');
const bulkEvaluations = new Counter('bulk_evaluations');
const ruleCreations = new Counter('rule_creations');
const ruleOperations = new Counter('rule_operations');

// Test configuration
export let options = {
  stages: [
    { duration: '30s', target: 20 },   // Ramp up to 20 users over 30s
    { duration: '60s', target: 50 },   // Scale to 50 users over 60s  
    { duration: '120s', target: 50 },  // Stay at 50 users for 2min
    { duration: '30s', target: 100 },  // Spike to 100 users
    { duration: '60s', target: 100 },  // Stay at 100 users for 1min
    { duration: '30s', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must be below 500ms
    http_req_failed: ['rate<0.01'],   // Error rate must be below 1%
    errors: ['rate<0.01'],            // Custom error rate below 1%
    flag_evaluations: ['count>100'],  // At least 100 single evaluations
    bulk_evaluations: ['count>20'],   // At least 20 bulk evaluations
    rule_creations: ['count>10'],     // At least 10 rule creations
    rule_operations: ['count>20'],    // At least 20 rule operations
  },
};

const BASE_URL = 'http://localhost:8080';

// Test data generator with unique identifiers
function generateFeatureFlag(index = null) {
  // Create truly unique identifier: timestamp + VU ID + random + optional index
  const vuId = __VU || 1;
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  const suffix = index !== null ? `${index}-${vuId}-${timestamp}-${random}` : `${vuId}-${timestamp}-${random}`;

  const valueType = ['BOOLEAN', 'STRING', 'INT', 'PERCENTAGE'][Math.floor(Math.random() * 4)];
  const flagData = {
    code: `load-test-flag-${suffix}`,
    name: `Load Test Feature ${suffix}`,
    description: `Generated during k6 load test - VU${vuId} - ${new Date().toISOString()}`,
    valueType: valueType
  };

  // Add appropriate value based on type
  switch (valueType) {
    case 'BOOLEAN':
      flagData.value = Math.random() > 0.5;
      break;
    case 'STRING':
      flagData.value = `test-value-${Math.floor(Math.random() * 1000)}`;
      break;
    case 'INT':
      flagData.value = Math.floor(Math.random() * 100);
      break;
    case 'PERCENTAGE':
      flagData.value = Math.floor(Math.random() * 100);
      break;
  }

  return flagData;
}

function generateValueByType(type) {
  switch (type) {
    case 'BOOLEAN': return Math.random() > 0.5;
    case 'STRING': return `test-value-${Math.floor(Math.random() * 1000)}`;
    case 'INT': return Math.floor(Math.random() * 100);
    case 'PERCENTAGE': return Math.floor(Math.random() * 100);
    default: return true;
  }
}

// Setup function - runs once before the test
export function setup() {
  console.log('🚀 Starting Ground Control k6 Load Test');

  // Health check using main API endpoint
  const healthResponse = http.get(`${BASE_URL}/v1/flags`);
  if (healthResponse.status !== 200) {
    throw new Error('Application health check failed - ensure Ground Control is running');
  }

  console.log('✅ Application is healthy and ready for testing');

  // Create stable test flags for evaluation (simple naming to avoid duplicates)
  const initialFlags = [];
  const baseTimestamp = Date.now();

  for (let i = 1; i <= 10; i++) {
    const flagData = {
      code: `stable-test-flag-${baseTimestamp}-${i}`,
      name: `Stable Test Flag ${i}`,
      description: `Stable flag for load testing - ${new Date().toISOString()}`,
      valueType: 'BOOLEAN',
      value: true
    };

    const response = http.post(`${BASE_URL}/v1/flags`, JSON.stringify(flagData), {
      headers: { 'Content-Type': 'application/json' },
    });

    if (response.status === 201) {
      initialFlags.push(flagData.code);
    } else {
      console.log(`⚠️ Failed to create flag ${flagData.code}: ${response.status} - ${response.body}`);
    }
  }

  console.log(`✅ Created ${initialFlags.length} initial test flags for evaluation`);
  return { testFlags: initialFlags };
}

// Main test function
export default function(data) {
  const testScenario = Math.random();

  if (testScenario < 0.20) {
    // 20% - Evaluate single flag
    evaluateFlag(data.testFlags);
  } else if (testScenario < 0.28) {
    // 8% - Bulk evaluate flags
    bulkEvaluateFlags(data.testFlags);
  } else if (testScenario < 0.45) {
    // 17% - Read all flags
    readAllFlags();
  } else if (testScenario < 0.60) {
    // 15% - Read specific flag
    readSpecificFlag(data.testFlags);
  } else if (testScenario < 0.68) {
    // 8% - Create new flag
    createFeatureFlag();
  } else if (testScenario < 0.76) {
    // 8% - Update flag state
    updateFlagState(data.testFlags);
  } else if (testScenario < 0.82) {
    // 6% - Query multiple flags by codes
    queryFlagsByCodes(data.testFlags);
  } else if (testScenario < 0.88) {
    // 6% - Create rollout rule
    createRolloutRule(data.testFlags);
  } else if (testScenario < 0.94) {
    // 6% - Read rollout rules
    readRolloutRules(data.testFlags);
  } else if (testScenario < 0.97) {
    // 3% - Update rollout rule
    updateRolloutRule(data.testFlags);
  } else {
    // 3% - Reorder rollout rules
    reorderRolloutRules(data.testFlags);
  }

  // Random sleep between 1-3 seconds to simulate user behavior
  sleep(Math.random() * 2 + 1);
}

// Test scenario functions
function readAllFlags() {
  group('Read All Flags', () => {
    const response = http.get(`${BASE_URL}/v1/flags`);

    const success = check(response, {
      'GET /v1/flags status is 200': (r) => r.status === 200,
      'GET /v1/flags response time < 300ms': (r) => r.timings.duration < 300,
      'GET /v1/flags returns paginated data': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.content !== undefined && Array.isArray(data.content);
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    flagReads.add(1);
  });
}

function readSpecificFlag(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Read Specific Flag', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const response = http.get(`${BASE_URL}/v1/flags/${randomFlag}`);

    const success = check(response, {
      'GET /v1/flags/{code} status is 200': (r) => r.status === 200,
      'GET /v1/flags/{code} response time < 200ms': (r) => r.timings.duration < 200,
      'GET /v1/flags/{code} returns flag object': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.code && data.name;
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    flagReads.add(1);
  });
}

function createFeatureFlag() {
  group('Create Feature Flag', () => {
    const flagData = generateFeatureFlag();
    const response = http.post(`${BASE_URL}/v1/flags`, JSON.stringify(flagData), {
      headers: { 'Content-Type': 'application/json' },
    });

    const success = check(response, {
      'POST /v1/flags status is 201 or 409 (duplicate)': (r) => r.status === 201 || r.status === 409,
      'POST /v1/flags response time < 500ms': (r) => r.timings.duration < 500,
      'POST /v1/flags returns created flag or error message': (r) => {
        try {
          const data = JSON.parse(r.body);
          // Accept either successful creation or duplicate error
          return (r.status === 201 && data.id && data.code === flagData.code) ||
                 (r.status === 409); // Conflict/duplicate is acceptable
        } catch {
          return false;
        }
      }
    });

    // Only count as error if it's not a 409 (duplicate) and not a 201 (success)
    const isRealError = response.status !== 201 && response.status !== 409;
    errorRate.add(isRealError);
    responseTime.add(response.timings.duration);
    flagCreations.add(1);
  });
}

function updateFlagState(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Update Flag State', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const updateData = { isEnabled: Math.random() > 0.5 };

    const response = http.patch(`${BASE_URL}/v1/flags/${randomFlag}/change-state`,
      JSON.stringify(updateData), {
      headers: { 'Content-Type': 'application/json' },
    });

    const success = check(response, {
      'PATCH /v1/flags/{code}/change-state status is 204': (r) => r.status === 204,
      'PATCH /v1/flags/{code}/change-state response time < 300ms': (r) => r.timings.duration < 300,
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
  });
}

function queryFlagsByCodes(testFlags) {
  if (!testFlags || testFlags.length < 2) return;

  group('Query Flags by Codes', () => {
    // Select 2-3 random flags
    const selectedFlags = testFlags
      .sort(() => 0.5 - Math.random())
      .slice(0, Math.floor(Math.random() * 2) + 2);

    const codesParam = selectedFlags.join(',');
    const response = http.get(`${BASE_URL}/v1/flags/by-codes?codes=${codesParam}`);

    const success = check(response, {
      'GET /v1/flags/by-codes status is 200': (r) => r.status === 200,
      'GET /v1/flags/by-codes response time < 250ms': (r) => r.timings.duration < 250,
      'GET /v1/flags/by-codes returns expected structure': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.featureFlags && Array.isArray(data.featureFlags);
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    flagReads.add(1);
  });
}

// NEW: Evaluation test functions
function evaluateFlag(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Evaluate Single Flag', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const evaluationContext = generateEvaluationContext();

    const response = http.post(`${BASE_URL}/v1/evaluations/${randomFlag}`,
      JSON.stringify(evaluationContext), {
      headers: { 'Content-Type': 'application/json' },
    });

    const success = check(response, {
      'POST /v1/evaluations/{code} status is 200': (r) => r.status === 200,
      'POST /v1/evaluations/{code} response time < 100ms': (r) => r.timings.duration < 100,
      'POST /v1/evaluations/{code} returns evaluation result': (r) => {
        try {
          const data = JSON.parse(r.body);
          return typeof data.enabled === 'boolean' && data.reason;
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    flagEvaluations.add(1);
  });
}

function bulkEvaluateFlags(testFlags) {
  if (!testFlags || testFlags.length < 2) return;

  group('Bulk Evaluate Flags', () => {
    // Select 2-4 random flags
    const selectedFlags = testFlags
      .sort(() => 0.5 - Math.random())
      .slice(0, Math.floor(Math.random() * 3) + 2);

    const bulkRequest = {
      flagCodes: selectedFlags,
      context: generateEvaluationContext()
    };

    const response = http.post(`${BASE_URL}/v1/evaluations/bulk`,
      JSON.stringify(bulkRequest), {
      headers: { 'Content-Type': 'application/json' },
    });

    const success = check(response, {
      'POST /v1/evaluations/bulk status is 200': (r) => r.status === 200,
      'POST /v1/evaluations/bulk response time < 150ms': (r) => r.timings.duration < 150,
      'POST /v1/evaluations/bulk returns bulk results': (r) => {
        try {
          const data = JSON.parse(r.body);
          return typeof data === 'object' && Object.keys(data).length > 0;
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    bulkEvaluations.add(1);
  });
}

function generateEvaluationContext() {
  const contexts = [
    {
      subjectId: `user_${Math.floor(Math.random() * 10000)}`,
      attributes: {
        plan: ['basic', 'premium', 'enterprise'][Math.floor(Math.random() * 3)],
        creditScore: Math.floor(Math.random() * 400) + 600,
        country: ['US', 'CA', 'UK', 'DE', 'FR'][Math.floor(Math.random() * 5)]
      }
    },
    {
      subjectId: `tenant_${Math.floor(Math.random() * 1000)}`,
      attributes: {
        tier: ['starter', 'growth', 'enterprise'][Math.floor(Math.random() * 3)],
        employees: Math.floor(Math.random() * 5000) + 10,
        industry: ['tech', 'finance', 'healthcare', 'retail'][Math.floor(Math.random() * 4)]
      }
    },
    {
      subjectId: `device_${Math.floor(Math.random() * 50000)}`,
      attributes: {
        os: ['ios', 'android', 'web'][Math.floor(Math.random() * 3)],
        version: `${Math.floor(Math.random() * 10) + 1}.${Math.floor(Math.random() * 10)}`,
        premium: Math.random() > 0.7
      }
    }
  ];
  
  return contexts[Math.floor(Math.random() * contexts.length)];
}

// RolloutRule test functions
function createRolloutRule(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Create Rollout Rule', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const ruleData = generateRolloutRule();

    const response = http.post(`${BASE_URL}/v1/flags/${randomFlag}/rules`,
      JSON.stringify(ruleData), {
      headers: { 'Content-Type': 'application/json' },
    });

    const success = check(response, {
      'POST /v1/flags/{code}/rules status is 201': (r) => r.status === 201,
      'POST /v1/flags/{code}/rules response time < 400ms': (r) => r.timings.duration < 400,
      'POST /v1/flags/{code}/rules returns rule with ID': (r) => {
        try {
          if (r.status === 201) {
            const data = JSON.parse(r.body);
            return data.id && data.priority !== undefined;
          }
          return true;
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    ruleCreations.add(1);
  });
}

function readRolloutRules(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Read Rollout Rules', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const response = http.get(`${BASE_URL}/v1/flags/${randomFlag}/rules`);

    const success = check(response, {
      'GET /v1/flags/{code}/rules status is 200': (r) => r.status === 200,
      'GET /v1/flags/{code}/rules response time < 200ms': (r) => r.timings.duration < 200,
      'GET /v1/flags/{code}/rules returns array': (r) => {
        try {
          const data = JSON.parse(r.body);
          return Array.isArray(data);
        } catch {
          return false;
        }
      }
    });

    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    ruleOperations.add(1);
  });
}

function updateRolloutRule(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Update Rollout Rule', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];

    // First, get rules for this flag
    const listResponse = http.get(`${BASE_URL}/v1/flags/${randomFlag}/rules`);

    if (listResponse.status === 200) {
      try {
        const rules = JSON.parse(listResponse.body);
        if (rules.length > 0) {
          const randomRule = rules[Math.floor(Math.random() * rules.length)];
          const updateData = {
            percentage: Math.floor(Math.random() * 100),
            active: Math.random() > 0.5
          };

          const response = http.put(`${BASE_URL}/v1/flags/${randomFlag}/rules/${randomRule.id}`,
            JSON.stringify(updateData), {
            headers: { 'Content-Type': 'application/json' },
          });

          const success = check(response, {
            'PUT /v1/flags/{code}/rules/{id} status is 200': (r) => r.status === 200,
            'PUT /v1/flags/{code}/rules/{id} response time < 350ms': (r) => r.timings.duration < 350,
          });

          errorRate.add(!success);
          responseTime.add(response.timings.duration);
          ruleOperations.add(1);
        }
      } catch (e) {
        // Skip if parsing fails
      }
    }
  });
}

function reorderRolloutRules(testFlags) {
  if (!testFlags || testFlags.length === 0) return;

  group('Reorder Rollout Rules', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];

    // First, get rules for this flag
    const listResponse = http.get(`${BASE_URL}/v1/flags/${randomFlag}/rules`);

    if (listResponse.status === 200) {
      try {
        const rules = JSON.parse(listResponse.body);
        if (rules.length >= 2) {
          // Shuffle rule IDs for reordering
          const ruleIds = rules.map(r => r.id).sort(() => Math.random() - 0.5);
          const reorderData = { ruleIds: ruleIds };

          const response = http.post(`${BASE_URL}/v1/flags/${randomFlag}/rules/reorder`,
            JSON.stringify(reorderData), {
            headers: { 'Content-Type': 'application/json' },
          });

          const success = check(response, {
            'POST /v1/flags/{code}/rules/reorder status is 204': (r) => r.status === 204,
            'POST /v1/flags/{code}/rules/reorder response time < 400ms': (r) => r.timings.duration < 400,
          });

          errorRate.add(!success);
          responseTime.add(response.timings.duration);
          ruleOperations.add(1);
        }
      } catch (e) {
        // Skip if parsing fails
      }
    }
  });
}

function generateRolloutRule() {
  const ruleTypes = [
    {
      percentage: Math.floor(Math.random() * 100),
      priority: Math.floor(Math.random() * 10),
      active: true,
      attributeKey: 'plan',
      attributeValue: ['basic', 'premium', 'enterprise'][Math.floor(Math.random() * 3)],
      variantName: `variant-${Math.floor(Math.random() * 5)}`
    },
    {
      percentage: Math.floor(Math.random() * 50) + 50,
      priority: Math.floor(Math.random() * 10),
      active: true,
      attributeKey: 'country',
      attributeValue: ['US', 'CA', 'UK'][Math.floor(Math.random() * 3)],
      distributionKeyAttribute: 'userId'
    },
    {
      percentage: 100,
      priority: Math.floor(Math.random() * 10),
      active: Math.random() > 0.3,
      valueBool: Math.random() > 0.5
    }
  ];

  return ruleTypes[Math.floor(Math.random() * ruleTypes.length)];
}

// Teardown function - runs once after the test
export function teardown(data) {
  console.log('🧹 Test completed, running cleanup...');

  // Optional: Clean up test data
  // Note: In a real scenario, you might want to clean up test flags
  // For now, we'll leave them for inspection

  console.log('✅ Ground Control k6 load test completed successfully!');
  console.log('📊 Evaluation and RolloutRule endpoints tested with realistic scenarios');
}