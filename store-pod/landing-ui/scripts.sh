#!/bin/bash
# List build outputs (handy before a clean).
find libs -name dist -type d -maxdepth 2
find storefront -name .next -type d -maxdepth 1
