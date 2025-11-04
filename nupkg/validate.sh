#!/bin/bash
# Validation script for NuGet package build
set -e

echo "========================================"
echo "Kafka NuGet Package Validation Script"
echo "========================================"
echo

# Check if nuspec file exists
if [ ! -f "nupkg/kafka.nuspec" ]; then
    echo "ERROR: nupkg/kafka.nuspec not found!"
    exit 1
fi
echo "✓ Found kafka.nuspec"

# Validate XML syntax
if command -v xmllint &> /dev/null; then
    echo "Validating XML syntax..."
    xmllint --noout nupkg/kafka.nuspec 2>&1
    echo "✓ kafka.nuspec is valid XML"
else
    echo "⚠ xmllint not found, skipping XML validation"
fi

# Check for required metadata fields
echo
echo "Checking required metadata fields..."
required_fields=("id" "version" "authors" "description")
for field in "${required_fields[@]}"; do
    if grep -q "<$field>" nupkg/kafka.nuspec; then
        echo "✓ Found <$field>"
    else
        echo "ERROR: Missing <$field> in nuspec"
        exit 1
    fi
done

# Check if extract directory would be created during build
echo
echo "Checking build prerequisites..."
if [ -d "extract" ]; then
    echo "⚠ extract/ directory exists (will be used during packaging)"
else
    echo "ℹ extract/ directory will be created during build"
fi

# Check gradle.properties for version
version=""
nuget_version=""
if [ -f "gradle.properties" ]; then
    version=$(grep "^version=" gradle.properties | cut -d'=' -f2)
    echo "✓ Found version in gradle.properties: $version"
    
    # Convert to NuGet format
    nuget_version=$(echo $version | sed 's/-SNAPSHOT/-preview/')
    echo "  → NuGet version would be: $nuget_version"
else
    echo "ERROR: gradle.properties not found!"
    exit 1
fi

echo
echo "========================================"
echo "Validation completed successfully! ✓"
echo "========================================"
echo
echo "To build the package:"
echo "  1. ./gradlew releaseTarGz"
echo "  2. Extract the tarball to ./extract/kafka/"
echo "  3. nuget pack ./nupkg/kafka.nuspec -Version $nuget_version -basepath ."
echo
