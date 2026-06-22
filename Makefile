include .env
export

DEPLOY_SCRIPT = ./.scripts/deploy.sh
MVN ?= mvn

include ~/.make/git.mk

.PHONY: build clean run test test-cli test-contract test-infrastructure test-tdd test-tu test-unit version

build:
	$(MVN) clean package

run:
	java -jar target/*.jar

test:
	$(MVN) test

test-cli:
	$(MVN) test -Dgroups=cli

test-contract:
	$(MVN) test -Dgroups=contract

test-infrastructure:
	$(MVN) test -Dgroups=infrastructure

test-tdd: test-contract

test-tu: test-unit

test-unit:
	$(MVN) test -Dgroups=unit

clean:
	$(MVN) clean

version:
ifndef VERSION
	$(error VERSION is required. Usage: make version VERSION=0.2.0)
endif
	$(MVN) versions:set -DnewVersion=$(VERSION)
	$(MVN) versions:commit
