/**
 * Shared module containing cross-module API contracts (Value Objects and Domain Events).
 * <p>
 * This module is marked as OPEN to allow other modules (toggles, analytics) to access
 * the shared types in the api package without violating Spring Modulith's encapsulation rules.
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.product.ground_control.shared;
