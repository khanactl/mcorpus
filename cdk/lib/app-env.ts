#!/usr/bin/env node

/**
 * The supported application environments.
 */
export enum AppEnv {
  /** development (staging / non-production) */
  DEV = 'DEV', 
  /** production */
  PRD = 'PRD', 
  /** shared (used by both production and non-production) */
  SHARED = 'SHARED', 
}
