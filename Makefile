include .env
export

DEPLOY_SCRIPT = ./.scripts/deploy.sh
MVN ?= mvn

ifeq ($(RELEASE_WRAPPER),1)
include ~/.make/git.mk
else ifneq ($(filter release,$(MAKECMDGOALS)),release)
include ~/.make/git.mk
endif

.PHONY: build clean release run snapshot test test-cli test-contract test-infrastructure test-tdd test-tu test-unit version install

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

ifneq ($(RELEASE_WRAPPER),1)
ifeq ($(filter release,$(MAKECMDGOALS)),release)
release:
ifndef VERSION
	$(error VERSION is required. Usage: make release VERSION=0.2.0)
endif
	$(MAKE) RELEASE_WRAPPER=1 release VERSION=$(VERSION)
	$(MAKE) snapshot VERSION=$$(echo "$(VERSION)" | awk -F. '{ printf "%d.%d.0", $$1, $$2 + 1 }')
endif
endif

snapshot:
ifndef VERSION
	$(error VERSION is required. Usage: make snapshot VERSION=0.3.0)
endif
	$(MVN) versions:set -DnewVersion=$(VERSION)-SNAPSHOT
	$(MVN) versions:commit
	git add pom.xml
	git commit -m "chore(snapshot): Open v$(VERSION)-SNAPSHOT"
	git push

version:
ifndef VERSION
	$(error VERSION is required. Usage: make version VERSION=0.2.0)
endif
	$(MVN) versions:set -DnewVersion=$(VERSION)
	$(MVN) versions:commit

install: build
	./.scripts/install.sh
