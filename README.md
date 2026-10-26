# Xport

[![Build](https://github.com/leeturner/xport/actions/workflows/build.yml/badge.svg)](https://github.com/leeturner/xport/actions/workflows/build.yml)
[![Licence](https://img.shields.io/github/license/leeturner/xport)](LICENSE)

A command line tool, written in Kotlin and Micronaut, that converts an X/Twitter data export into Markdown files for
[Hugo](https://gohugo.io/). Each tweet becomes its own file with its original timestamp, and its images and videos
come along too. I used it to bring my old tweets over to the [microblog on my site](https://leeturner.me/microblog/).

## Features

- Turns every tweet in the archive into a Hugo Markdown file with front matter
- Keeps the original timestamps, for both the file names and the post dates
- Copies images and videos from the archive and links them in each post
- Expands `t.co` short links back to the original URLs
- Optionally leaves out replies to other people's tweets

## Requirements

- Java 25 or later

## Install

There are no releases yet, so build it from source:

```bash
git clone https://github.com/leeturner/xport.git
cd xport
./gradlew installDist
```

This puts the `xport` command in `build/install/xport/bin`. To build a native executable instead, use
`./gradlew nativeCompile` with GraalVM.

## Usage

```bash
xport [OPTIONS] <archive-directory> <output-directory>
```

| Argument or option | Meaning |
|---|---|
| `<archive-directory>` | The unzipped Twitter archive, containing the `data` folder |
| `<output-directory>` | Where to write the Markdown. It must exist and be empty |
| `-a, --author` | Author name for the generated files |
| `-f, --filter-replies` | Leave out tweets that reply to another tweet |
| `-m, --media-path` | Path to put in front of media URLs in the generated files |
| `-v, --verbose` | Show what is being processed |
| `-h, --help` | Show help and exit |
| `-V, --version` | Show the version and exit |

For example, to convert an archive with your name as the author, without replies, and with media served from
`/images/tweets`:

```bash
xport -a "Lee Turner" -f -m "/images/tweets" ~/Downloads/twitter-archive ~/Documents/my-tweets
```

## How it works

Xport processes your Twitter archive through a pipeline of tasks:

1. Copies the tweet.js file from the archive
2. Converts the JavaScript format to JSON
3. Transforms the JSON to Hugo-compatible Markdown
   - Parses and formats tweet timestamps for filenames and Hugo front matter
   - Expands shortened Twitter URLs (t.co links) to their original full URLs
   - Converts media URLs to proper markdown image references or HTML video tags
   - Adds reply context with links to original tweets for replies
   - Applies custom author name and media paths if provided
4. Copies media files (images, videos) from the archive
5. Cleans up temporary files

The resulting Markdown files include Hugo front matter with the tweet's timestamp and content, ready to drop into a Hugo site.

## Output

Each tweet is converted to a Markdown file with the following structure:

```markdown
---
title: YYYY-MM-DDTHH-MM
date: YYYY-MM-DDTHH:MM:SS+0000
author: Your Name  # Set with the -a, --author option
showtoc: false
comments: true
---

Tweet content goes here

![](/path/to/media)  # The path prefix is set with the -m, --media-path option
```

## Licence

Apache 2.0. See [LICENSE](LICENSE).
