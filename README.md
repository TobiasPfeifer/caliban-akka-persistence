# CAP - Caliban Akka Persistence

Just a small sample project to integrate and play around with:
* [Akka Typed](https://github.com/akka/akka)
    * akka-persistence-typed for Eventsourcing
    * akka-http as HTTP-Server
* [caliban](https://github.com/ghostdogpr/caliban)
    * [graphql](https://graphql.org/)
* [ZIO](https://github.com/zio/zio)
* [chimney](https://github.com/scalalandio/chimney)
    * Type-safe data transformation

### GraphQL Api Usage
Increment / Decrement the counter:
```
mutation {
  increment(n: 5) {
      count
  } 
}
```
```
mutation {
  decrement(n: 5) {
      count
  }
}
```

Querying the counter:
```
{
  count {
    count
    lastUpdateTimestamp
    description
  }
}
```

Setting a description on the counter:
```
mutation {
  setDescription(description: "I'm overwriting the description ignoring the current count") {
    count
    description
  }
}
```

```
mutation {
  setDescription(ifCountMatching: 10, description: "I'm only overwriting the description if the current count is 10") {
    count
    description
  }
}
```
