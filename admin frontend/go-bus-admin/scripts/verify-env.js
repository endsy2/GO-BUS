#!/usr/bin/env node

/**
 * Environment Variables Verification Script
 * Run this to check if your environment variables are properly configured
 */

console.log('\n🔍 Environment Variables Check\n');
console.log('='.repeat(60));

// Check Node environment
console.log('\n📦 Node Environment:');
console.log(`   NODE_ENV: ${process.env.NODE_ENV || 'not set'}`);

// Check React App variables
console.log('\n⚛️  React App Variables:');

const requiredVars = [
  'REACT_APP_BASE_URL',
  'REACT_APP_WS_URL'
];

let allSet = true;

requiredVars.forEach(varName => {
  const value = process.env[varName];
  const status = value ? '✅' : '❌';
  console.log(`   ${status} ${varName}: ${value || 'NOT SET'}`);
  
  if (!value) {
    allSet = false;
  }
});

// Check for common issues
console.log('\n🔎 Validation:');

const baseUrl = process.env.REACT_APP_BASE_URL;
const wsUrl = process.env.REACT_APP_WS_URL;

if (baseUrl) {
  // Check protocol
  if (baseUrl.startsWith('http://') && !baseUrl.includes('localhost')) {
    console.log('   ⚠️  BASE_URL uses http:// (should use https:// in production)');
  } else if (baseUrl.startsWith('https://')) {
    console.log('   ✅ BASE_URL uses https://');
  }
  
  // Check trailing slash
  if (baseUrl.endsWith('/')) {
    console.log('   ⚠️  BASE_URL has trailing slash (should remove it)');
  } else {
    console.log('   ✅ BASE_URL has no trailing slash');
  }
  
  // Check localhost
  if (baseUrl.includes('localhost')) {
    console.log('   ℹ️  BASE_URL points to localhost (OK for development)');
  }
}

if (wsUrl) {
  // Check WebSocket protocol
  if (wsUrl.startsWith('ws://') && !wsUrl.includes('localhost')) {
    console.log('   ⚠️  WS_URL uses ws:// (should use wss:// in production)');
  } else if (wsUrl.startsWith('wss://')) {
    console.log('   ✅ WS_URL uses wss://');
  }
  
  // Check localhost
  if (wsUrl.includes('localhost')) {
    console.log('   ℹ️  WS_URL points to localhost (OK for development)');
  }
}

// Summary
console.log('\n' + '='.repeat(60));

if (allSet) {
  console.log('✅ All required environment variables are set!\n');
  process.exit(0);
} else {
  console.log('❌ Some environment variables are missing!\n');
  console.log('💡 Quick Fix:');
  console.log('   1. Create .env file in project root');
  console.log('   2. Add these lines:');
  console.log('      REACT_APP_BASE_URL=http://localhost:8080');
  console.log('      REACT_APP_WS_URL=http://localhost:8080/bus-service/ws/bus');
  console.log('   3. Restart dev server: npm start\n');
  console.log('📖 See ENVIRONMENT_VARIABLES_FIX.md for detailed help\n');
  process.exit(1);
}
