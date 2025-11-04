# Apache Kafka NuGet Package

This directory contains the NuGet package specification for Apache Kafka.

## Downloading the Package

The Apache Kafka package is published to GitHub Packages and can be downloaded using NuGet.

### Prerequisites

- NuGet CLI or compatible package manager
- GitHub account with access to this repository

### Setup

1. Add the GitHub Packages source to your NuGet configuration:

```bash
nuget sources Add -Name github -Source "https://nuget.pkg.github.com/etn-utilities/index.json" -Username YOUR_GITHUB_USERNAME -Password YOUR_GITHUB_TOKEN
```

2. Install the Apache Kafka package:

```bash
nuget install Apache.Kafka
```

Or if using .NET CLI:

```bash
dotnet add package Apache.Kafka --source github
```

## Package Contents

The NuGet package includes:

- **bin/** - All Kafka executables and scripts (both Unix shell and Windows batch files)
- **libs/** - All required Java libraries (JAR files)
- **config/** - Sample configuration files for Kafka brokers, producers, consumers, and Connect
- **licenses/** - Third-party license files
- **site-docs/** - Documentation
- **LICENSE** - Apache License 2.0
- **NOTICE** - Apache Software Foundation notices

## Building the Package Manually

To build the NuGet package from source:

1. Build the Kafka distribution:
```bash
./gradlew releaseTarGz
```

2. Extract the distribution:
```bash
ARCHIVE_PATH=$(find . -name "kafka_*.tgz" | head -1)
mkdir -p ./extract/kafka
tar -xzf $ARCHIVE_PATH -C ./extract/kafka --strip-components=1
```

3. Pack the NuGet package:
```bash
VERSION=$(grep "^version=" gradle.properties | cut -d'=' -f2)
NUGET_VERSION=$(echo $VERSION | sed 's/-SNAPSHOT/-preview/')
nuget pack ./nupkg/kafka.nuspec -Version $NUGET_VERSION -basepath .
```

## Publishing the Package

The package is automatically published via GitHub Actions when the "Publish to NuGet" workflow is manually triggered.

To publish manually:

```bash
nuget push Apache.Kafka.*.nupkg -Source github -ApiKey YOUR_GITHUB_TOKEN
```

## Using Kafka

After installing the package, the Kafka tools will be available in the `tools` directory of the package installation location.

For example, to start a Kafka broker:

```bash
# On Unix/Linux/macOS
./tools/bin/kafka-server-start.sh ./tools/config/server.properties

# On Windows
.\tools\bin\windows\kafka-server-start.bat .\tools\config\server.properties
```

For more information about using Apache Kafka, visit:
- Official documentation: https://kafka.apache.org/documentation/
- Quick start guide: https://kafka.apache.org/quickstart

## Version Information

The version number follows the Apache Kafka release versioning:
- Release versions: `X.Y.Z` (e.g., `4.2.0`)
- Development versions: `X.Y.Z-preview` (e.g., `4.2.0-preview`)

Note: `-SNAPSHOT` versions from Gradle are converted to `-preview` for NuGet compatibility.
