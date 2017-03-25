# Violations comments to bitbucket cloud comments lib

[![Build Status](https://travis-ci.org/ghusse/violations-comments-to-bitbucket-cloud-lib.svg?branch=master)](https://travis-ci.org/ghusse/violations-comments-to-bitbucket-cloud-lib)[![Coverage Status](https://coveralls.io/repos/github/ghusse/violations-comments-to-bitbucket-cloud-lib/badge.svg?branch=master)](https://coveralls.io/github/ghusse/violations-comments-to-bitbucket-cloud-lib?branch=master)

This library is currently under development and not functional.

Library to allow CI servers to publish comments about violations introduced in Pull Requests.

It uses [violation-comments-lib](https://github.com/tomasbjerre/violation-comments-lib).

## Running integration tests

You need to create a property file with these parameters:

`src/test-integration/java/config.properties`
```
username=[A valid username]
password=[Application password generated for your username, with rights on pull requests]
```

## License

License under MIT License, Copyright 2017 Guillaume Gautreau
