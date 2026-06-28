const fs = require('fs');
const path = require('path');

const files = [
  'src/features/reports/components/CustomerReportsTab/CustomerReportsTab.jsx',
  'src/features/reports/components/BusReportsTab/BusReportsTab.jsx'
];

files.forEach(filePath => {
  let content = fs.readFileSync(filePath, 'utf8');
  
  // Replace all Input type="date" with DatePicker
  content = content.replace(
    /<Input\s+type="date"\s+value=\{([^}]+)\}\s+onChange=\{\(e\) => ([^(]+)\(\{ \.\.\.([^,]+), ([^:]+): e\.target\.value \}\)\}\s*\/>/g,
    '<DatePicker\n                value={$1}\n                onChange={(value) => $2({ ...$3, $4: value })}\n                placeholder="Select date"\n              />'
  );
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log(`Updated: ${filePath}`);
});

console.log('All files updated successfully!');
