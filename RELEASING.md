# Releasing

## Prerequisites

To be able to publish release you need to:

* Have an account on following Jira https://issues.sonatype.org
* Have permissions to publish to com.amadeus project (see https://issues.sonatype.org/browse/OSSRH-53515)
* Add these credentials in Maven settings.xml:

```xml
<server>
  <id>ossrh</id>
  <username>user</username>
  <password>password</password>
</server>
```

* Setup local necessary keys to have artifact signing enabled (see <https://central.sonatype.org/pages/ossrh-guide.html>, <https://central.sonatype.org/pages/working-with-pgp-signatures.html>, and <http://maven.apache.org/plugins/maven-gpg-plugin/usage.html> )


## Creating a release

```sh
./release.sh 1.0.0
```

Then push the commits and tags:

```sh
git push origin --tags
```

## Artifacts promotion

```sh
mvn nexus-staging:release
```

Or go to https://oss.sonatype.org/, sign with your oss credentials and follow steps from <https://central.sonatype.org/pages/ossrh-guide.html>.

## Changelog

Add the changelog associated to tag on GitHub releases tab.
