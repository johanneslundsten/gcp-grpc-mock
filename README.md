## GCP Mock

You can use this to mock the grpc apis of GCP that are published on [Google Api Repo](https://github.com/googleapis/googleapis). All apis are not added, if there is one that is missing you can just dive into the [Dockerfile](docker/Dockerfile) and follow the instructions.

## Using the Mock in a Junit Test
There is a [Testcontainer](https://testcontainers.com/) implementation you can use.
Will Look like this in your test.
```kotlin
    companion object {
        @JvmStatic
        @Container
        private val container = GcpMockContainer()

        @JvmStatic
        @BeforeAll
        fun start() {
            container.start()
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            container.stop()
        }
    }

    private val kmsMock = container.createKmsServiceMock()
    private val kmsClient = container.createKmsClient()
```

Have a look at the tests to see how the mock works in action.
