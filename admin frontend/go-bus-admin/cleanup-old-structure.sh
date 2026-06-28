#!/bin/bash

echo "🧹 Cleaning up old folder structure..."
echo ""
echo "⚠️  WARNING: This will delete the old components folder!"
echo "Make sure you have:"
echo "  1. Committed your current changes to git"
echo "  2. Verified the new structure works"
echo "  3. Tested the application"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Cleanup cancelled."
    exit 0
fi

echo ""
echo "Creating backup..."
timestamp=$(date +%Y%m%d_%H%M%S)
mkdir -p backups
cp -r src/components backups/components_backup_$timestamp 2>/dev/null
cp -r src/hooks backups/hooks_backup_$timestamp 2>/dev/null
cp -r src/context backups/context_backup_$timestamp 2>/dev/null
cp -r src/utils backups/utils_backup_$timestamp 2>/dev/null
cp -r src/locales backups/locales_backup_$timestamp 2>/dev/null

echo "✓ Backup created in backups/ folder"
echo ""
echo "Removing old folders..."

# Remove old component structure
rm -rf src/components/atoms
rm -rf src/components/molecules
rm -rf src/components/organisms
rm -rf src/components/pages
rm -rf src/components/ui
rm -f src/components/Dashboard.css
rm -f src/components/Dashboard.js
rm -f src/components/Sidebar.css
rm -f src/components/Sidebar.js

# Remove the components folder if empty
rmdir src/components 2>/dev/null

# Remove old folders that have been moved to shared/
rm -rf src/hooks
rm -rf src/context
rm -rf src/utils
rm -rf src/locales

echo "✓ Old folders removed"
echo ""
echo "✅ Cleanup complete!"
echo ""
echo "Your new structure:"
echo "  src/features/     - Feature modules"
echo "  src/shared/       - Shared resources"
echo "  src/services/     - Global services"
echo "  src/lib/          - Utilities"
echo ""
echo "Next steps:"
echo "  1. Run: npm run build"
echo "  2. Fix any import errors"
echo "  3. Run: npm start"
echo "  4. Test all features"
