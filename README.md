# amjil

FIXME: description

## Installation

Download from http://example.com/FIXME.

## Usage


    $ (require '[amjil.scp :as scp])
    $ (def remote {:host "10.220.5.71" :ssh-port 22 :user "root" :password "password"})
    $ (scp/upload "project.clj" "./" remote)
    $ (scp/download "project.clj" "./" remote)

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
