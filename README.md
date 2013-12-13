bootserver
==========

Partial DHCP/BOOTP/TFTP server, sufficient for serving PXE network installers
for most Linux distros.  Written in Java so that it can run on
Windows/Linux/Android/BSD without much fuss.

On Linux/Mac:
The `./build` script will compile the application, using `javac`.
The `./run` script will then launch the application, using `java`
Specify the `-d` switch to `./run` to launch in debug mode.
* The server requires privileges to listen on ports <1024, so you will
  probably need to run as root.

On Windows:
The run/build scripts can probably be converted to `.cmd` batch files with
minimal effort.
Otherwise, either use Cygwin to provide `bash`, or add the source tree to an
Eclipse project and compile/run from the IDE.
This application was actually developed on Windows...

On Android:
The server requires root access, so you will need a rooted phone.
Either launch it from Terminal Emulator, or write an nice little app that
launches the server class as root,
and create a nice interface to wrap `config.conf` while you're at it.

See `config.help` and optionally the program source code for documentation on
the `config.conf` file.
