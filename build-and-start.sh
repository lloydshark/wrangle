#!/bin/bash

npm run release

clj -A:server -X lloydshark.wrangler.main/run
