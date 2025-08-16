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
  },
};

const BASE_URL = 'http://localhost:8080';

// Test data generator
function generateFeatureFlag(index = null) {
  const suffix = index !== null ? index : Math.floor(Math.random() * 10000);
  return {
    code: `load-test-flag-${suffix}`,
    name: `Load Test Feature ${suffix}`,
    description: `Generated during k6 load test - ${new Date().toISOString()}`,
    enabled: Math.random() > 0.3, // 70% enabled flags
    valueType: ['BOOLEAN', 'STRING', 'INT', 'PERCENTAGE'][Math.floor(Math.random() * 4)],
    value: generateValueByType(['BOOLEAN', 'STRING', 'INT', 'PERCENTAGE'][Math.floor(Math.random() * 4)])
  };
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
  const healthResponse = http.get(`${BASE_URL}/flags`);
  if (healthResponse.status !== 200) {
    throw new Error('Application health check failed - ensure Ground Control is running');
  }
  
  console.log('✅ Application is healthy and ready for testing');
  
  // Create some initial test data
  const initialFlags = [];
  for (let i = 1; i <= 10; i++) {
    const flagData = generateFeatureFlag(i);
    const response = http.post(`${BASE_URL}/flags`, JSON.stringify(flagData), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (response.status === 201) {
      initialFlags.push(flagData.code);
    }
  }
  
  console.log(`✅ Created ${initialFlags.length} initial test flags`);
  return { testFlags: initialFlags };
}

// Main test function
export default function(data) {
  const testScenario = Math.random();
  
  if (testScenario < 0.25) {
    // 25% - Evaluate single flag (NEW!)
    evaluateFlag(data.testFlags);
  } else if (testScenario < 0.35) {
    // 10% - Bulk evaluate flags (NEW!)
    bulkEvaluateFlags(data.testFlags);
  } else if (testScenario < 0.55) {
    // 20% - Read all flags
    readAllFlags();
  } else if (testScenario < 0.75) {
    // 20% - Read specific flag
    readSpecificFlag(data.testFlags);
  } else if (testScenario < 0.85) {
    // 10% - Create new flag
    createFeatureFlag();
  } else if (testScenario < 0.95) {
    // 10% - Update flag state
    updateFlagState(data.testFlags);
  } else {
    // 5% - Query multiple flags by codes
    queryFlagsByCodes(data.testFlags);
  }
  
  // Random sleep between 1-3 seconds to simulate user behavior
  sleep(Math.random() * 2 + 1);
}

// Test scenario functions
function readAllFlags() {
  group('Read All Flags', () => {
    const response = http.get(`${BASE_URL}/flags`);
    
    const success = check(response, {
      'GET /flags status is 200': (r) => r.status === 200,
      'GET /flags response time < 300ms': (r) => r.timings.duration < 300,
      'GET /flags returns array': (r) => {
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
    flagReads.add(1);
  });
}

function readSpecificFlag(testFlags) {
  if (!testFlags || testFlags.length === 0) return;
  
  group('Read Specific Flag', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const response = http.get(`${BASE_URL}/flags/${randomFlag}`);
    
    const success = check(response, {
      'GET /flags/{code} status is 200': (r) => r.status === 200,
      'GET /flags/{code} response time < 200ms': (r) => r.timings.duration < 200,
      'GET /flags/{code} returns flag object': (r) => {
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
    const response = http.post(`${BASE_URL}/flags`, JSON.stringify(flagData), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    const success = check(response, {
      'POST /flags status is 201': (r) => r.status === 201,
      'POST /flags response time < 500ms': (r) => r.timings.duration < 500,
      'POST /flags returns created flag': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.id && data.code === flagData.code;
        } catch {
          return false;
        }
      }
    });
    
    errorRate.add(!success);
    responseTime.add(response.timings.duration);
    flagCreations.add(1);
  });
}

function updateFlagState(testFlags) {
  if (!testFlags || testFlags.length === 0) return;
  
  group('Update Flag State', () => {
    const randomFlag = testFlags[Math.floor(Math.random() * testFlags.length)];
    const updateData = { isEnabled: Math.random() > 0.5 };
    
    const response = http.patch(`${BASE_URL}/flags/${randomFlag}/change-state`, 
      JSON.stringify(updateData), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    const success = check(response, {
      'PATCH /flags/{code}/change-state status is 204': (r) => r.status === 204,
      'PATCH /flags/{code}/change-state response time < 300ms': (r) => r.timings.duration < 300,
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
    const response = http.get(`${BASE_URL}/flags/by-codes?codes=${codesParam}`);
    
    const success = check(response, {
      'GET /flags/by-codes status is 200': (r) => r.status === 200,
      'GET /flags/by-codes response time < 250ms': (r) => r.timings.duration < 250,
      'GET /flags/by-codes returns expected structure': (r) => {
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
    
    const response = http.post(`${BASE_URL}/evaluations/${randomFlag}`, 
      JSON.stringify(evaluationContext), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    const success = check(response, {
      'POST /evaluations/{code} status is 200': (r) => r.status === 200,
      'POST /evaluations/{code} response time < 100ms': (r) => r.timings.duration < 100,
      'POST /evaluations/{code} returns evaluation result': (r) => {
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
    
    const response = http.post(`${BASE_URL}/evaluations/bulk`, 
      JSON.stringify(bulkRequest), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    const success = check(response, {
      'POST /evaluations/bulk status is 200': (r) => r.status === 200,
      'POST /evaluations/bulk response time < 150ms': (r) => r.timings.duration < 150,
      'POST /evaluations/bulk returns bulk results': (r) => {
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

// Teardown function - runs once after the test
export function teardown(data) {
  console.log('🧹 Test completed, running cleanup...');
  
  // Optional: Clean up test data
  // Note: In a real scenario, you might want to clean up test flags
  // For now, we'll leave them for inspection
  
  console.log('✅ Ground Control k6 load test completed successfully!');
  console.log('📊 New evaluation endpoints tested with realistic user contexts');
}