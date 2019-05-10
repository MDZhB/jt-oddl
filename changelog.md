# Changelog

## [1.0.1] - 2019-05-10
### Added
- `ODDLToken`s now carry line and column information. This is exposed using the `getRow()` and `getCol()` methods.
### Changed
- `DelimiterToken`s and `BoolToken`s are no longer necessarily equal by identity if they are also equal by value.