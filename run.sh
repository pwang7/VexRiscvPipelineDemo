#! /bin/sh

set -o errexit
set -o nounset
set -o xtrace

CI_ENV="${CI_ENV:-false}"
MILL_VERSION="0.9.7"

if [ ! -f mill ]; then
  curl -L https://github.com/com-lihaoyi/mill/releases/download/$MILL_VERSION/$MILL_VERSION > mill && chmod +x mill
fi

./mill version

# Generate IDEA config
# ./mill mill.scalalib.GenIdea/idea

# Run build and simulation
./mill pipeline.runMain pipeline.demo.PipelineDemoSim

# Check format and lint
if [ "$CI_ENV" = "true" ]; then
  ./mill pipeline.checkFormat
  # ./mill pipeline.fix --check
else
  ./mill mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources
  # ./mill pipeline.fix
fi

