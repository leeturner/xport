# Xport - convert your X/Twitter archive to hugo compatible Markdown

Xport is a command-line tool built with Kotlin and Micronaut that processes Twitter data exports, converting them from 
JavaScript format to standard JSON and then to Hugo-compatible Markdown.

## Features

- Converts Twitter archive data from JavaScript format to JSON
- Transforms tweets into Hugo-compatible Markdown files
- Preserves tweet media (images, videos)
- Maintains original tweet timestamps
- Provides verbose output option for detailed processing information

## Installation

### Prerequisites

- Java 21 or higher
- Gradle (for building from source)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/xport.git
   cd xport
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Create a native executable (optional):
   ```bash
   ./gradlew nativeCompile
   ```

## Usage

```bash
xport [OPTIONS] <archive-directory> <output-directory>
```

### Arguments

- `<archive-directory>`: Path to the Twitter archive directory (containing the "data" folder)
- `<output-directory>`: Path to the output directory (must exist and be empty)

### Options

- `-v, --verbose`: Enable verbose output
- `-h, --help`: Show help message and exit

### Example

```bash
xport --v ~/Downloads/twitter-archive ~/Documents/my-tweets
```

## How It Works

Xport processes your Twitter archive through a pipeline of tasks:

1. Copies the tweet.js file from the archive
2. Converts the JavaScript format to JSON
3. Transforms the JSON to Hugo-compatible Markdown
4. Copies media files (images, videos) from the archive
5. Cleans up temporary files

The resulting Markdown files include Hugo front matter with the tweet's timestamp and content, making them ready to use 
with Hugo static site generator.

## Output Format

Each tweet is converted to a Markdown file with the following structure:

```markdown
---
title: YYYY-MM-DDTHH-MM
date: YYYY-MM-DDTHH:MM:SS+0000
author: Lee Turner
showtoc: false
comments: true
---

Tweet content goes here

![](media_url) <!-- If the tweet contains media -->
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
