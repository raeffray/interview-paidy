# One-Frame Service Proxy

Disclaimer: I would like to emphasize that although I`m an experienced Java developer, I'm not familiar 
with Scala. That being said, there are a lot of room for improvements, specially on failure and matching handling.

In overall, I really enjoyed this first contact with scala.

Scala force you to think in a different way, particularly working with side effects in a more appropriate way.

### Requirements and Assumptions

1. The service returns an exchange rate when provided with 2 supported currencies

2. The rate should not be older than 5 minutes

#### Assumption and how it was addressed:

This requirement implicitly says that we can cache the rate coming from One-Service, which will drastically reduce the live rate retrieving from One-Service

A new Cache Service was introduced using Caffeine to cache retrieved rate and stored with a configurable TTL, in this case 5 minutes, but can be changed accordingly

3. The service should support at least 10,000 successful requests per day with 1 API token

#### Assumption and how it was addressed:

The new cache layer was introduced to address this requirement, but another extra layer was introduced, spinning up the new container as a Docker Swarm Stack, making easy to scaling up and down the replicas for this service.

### Other Assumptions

The original One-Service Service has the capability of receiving multiple pairs of currencies, but the specification for the proxy does not.

Since there is no hard requirement to follow this parity, I decided to leave as it is. 

I took advantage though of the fact that the One-Service is capable of receiving multiple pairs of currencies, and I used it to create a cache for all the possible arrangements of currencies, and not only the ones requested by the client.

As consequence, every five minutes (or whatever configured TTL) a request will fill up the cache and the next request will be served from the cache.

This of course introduce some overhead in the process, and we don`t have any information whether that would be beneficial to the customer or not. 

Due my lack of proficiency in Scala, I gave up on unit test for time sake.

###  Configuring the live interpreter for One-Service external service

 The configuration for the live service resides in ```src/main/resources/application.conf```

```
  rate-service = {
    cache = {
      ttl = 300
      max-size = 100
    },
    lookup-service = {
      endpoint = "http://localhost:8080",
      path = "/rates",
      authentication-token-keyword = "token",
      authentication-token-value = "10dc303535874aeccc86a8251e6992f5"
      service-timeout = 3
    }
  }
  ```

Where

#### Cache
```ttl``` is the time-to-live for the cache keys, the cache age in seconds

```max-size``` the max size of the cache. Has to be equal the amount of the possible arrangements for the currencies.
expressed by ```A p,n = n! /(n-p)!```

#### Lookup service (One-Service)

```endpoint``` Endpoint for the service. e.g: http:&#8203;//localhost:8080

```path``` Path for the service. e.g:"/rates",

```authentication-token-keyword``` The token key word for one-service authentication

```authentication-token-value``` the bearer token. e.g: "10dc303535874aeccc86a8251e6992f5"

```service-timeout = 3``` The service Timeout when trying to connect

## Creating the one service stack,

please run ```backing-service/$ sh create-stack.sh```. It will create a initial stack with 3 replicas.

Scaling replicas up and down:

finding the services: ```docker stack services one-frame-service```

change the replicas set to 2: ```docker service scale one-frame-service_rate-service=2```

listing the replicas: ```docker service ps one-frame-service_rate-service```