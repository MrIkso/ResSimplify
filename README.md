# ResSimplify


[![GitHub release](https://img.shields.io/github/v/release/MrIkso/ResSimplify)](https://github.com/MrIkso/ResSimplify/releases) [![License](https://img.shields.io/github/license/MrIkso/ResSimplify?color=blue)](LICENSE)

An open source deobfuscator resources in apk file.

This tool used this open source project
- [ArscBlamer](https://github.com/google/android-arscblamer) for parsing and editing resources.arsc files

## Usage

`java -jar res-simplify-[version]-all.jar --in [input apk] --out [output apk]`

Using a whitelist from a file to ignore resource names
1. Create file by name `whitelist.txt`
2. Place file in directory with the res-simplify-[version]-all.jar
3. Enter resource names to ignore

Example of content in `whitelist.txt`
```
app_name
project_id
```

## License

    Copyright (C) 2021 Mr Isko

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
