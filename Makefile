include .env
export

DEPLOY_SCRIPT = ./.scripts/deploy.sh
MVN ?= mvn

include ~/.make/git.mk

.PHONY: build clean run test version

build:
	$(MVN) clean package

run:
	java -jar target/*.jar

test:
	$(MVN) test

clean:
	$(MVN) clean

version:
ifndef VERSION
	$(error VERSION is required. Usage: make version VERSION=0.2.0)
endif
	$(MVN) versions:set -DnewVersion=$(VERSION)
	$(MVN) versions:commit
