
# Register Trust Details Frontend

This service is responsible for collecting details about the trust a user is trying to register.

To run locally using the micro-service provided by the service manager:

```bash
sm2 --start TRUSTS_ALL
```

or

```bash
sm2 --start REGISTER_TRUST_ALL
```
---
## Testing the service
Run unit and integration tests before raising a PR to ensure your code changes pass the Jenkins pipeline. This runs all the unit tests and integration tests with scalastyle and checks for dependency updates:

```bash
./run_all_tests.sh
```

### UI Tests
Start up service in SM2 as shown above then:

```bash
./run_local_register_trust_details.sh
```
from trusts-acceptance-tests repository.

---

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 8842 but is defaulted to that in build.sbt):

```bash
sbt run
```

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
