# k8s-selector-java
This is k8s-label-selector tool based on jdk in java, it provide same function as [k8s-label-selector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)

### Syntax and character set
_Labels_ are key/value pairs. Valid label keys have two segments: an optional prefix and name, separated by a slash (`/`). The name segment is required and must be 63 characters or less, beginning and ending with an alphanumeric character (`[a-z0-9A-Z]`) with dashes (`-`), underscores (`_`), dots (`.`), and alphanumerics between. The prefix is optional. If specified, the prefix must be a DNS subdomain: a series of DNS labels separated by dots (`.`), not longer than 253 characters in total, followed by a slash (`/`).

Valid label value:
* must be 63 characters or less (can be empty),
* unless empty, must begin and end with an alphanumeric character (`[a-z0-9A-Z]`),
* could contain dashes (`-`), underscores (`_`), dots (`.`), and alphanumerics between.

### Label selectors
labels do not provide uniqueness. In general, we expect many objects to carry the same label(s). Via a _label selector_, the client/user can identify a set of objects.

##### Equality-based requirement

_Equality-_ or _inequality-based_ requirements allow filtering by label keys and values. Matching objects must satisfy all of the specified label constraints, though they may have additional labels as well.
Three kinds of operators are admitted `=`,`==`,`!=`. The first two represent _equality_ (and are synonyms), while the latter represents _inequality_. For example:

```
environment = production
tier != frontend
```

The former selects all resources with key equal to `environment` and value equal to `production`.
The latter selects all resources with key equal to `tier` and value distinct from `frontend`, and all resources with no labels with the `tier` key.
One could filter for resources in `production` excluding `frontend` using the comma operator: `environment=production,tier!=frontend`

##### Set-based requirement

_Set-based_ label requirements allow filtering keys according to a set of values. Three kinds of operators are supported: `in`,`notin` and `exists` (only the key identifier). For example:

```
environment in (production, qa)
tier notin (frontend, backend)
partition
!partition
```

* The first example selects all resources with key equal to `environment` and value equal to `production` or `qa`.
* The second example selects all resources with key equal to `tier` and values other than `frontend` and `backend`, and all resources with no labels with the `tier` key.
* The third example selects all resources including a label with key `partition`; no values are checked.
* The fourth example selects all resources without a label with key `partition`; no values are checked.

Similarly the comma separator acts as an _AND_ operator. So filtering resources with a `partition` key (no matter the value) and with `environment` different than  `qa` can be achieved using `partition,environment notin (qa)`.
The _set-based_ label selector is a general form of equality since `environment=production` is equivalent to `environment in (production)`; similarly for `!=` and `notin`.

_Set-based_ requirements can be mixed with _equality-based_ requirements. For example: `partition in (customerA, customerB),environment!=qa`.

### Time Complexity
* Selector parse process: O(n)
* Selector matches label process: O(n)
* Selector parse and matches together: O(n) 

### How to use it

```
#java
Map<String, String> labels = new HashMap<>();
labels.put("tier", "backend");
labels.put("environment", "production");

InternalSelector selector = Selector.parse("environment = production");
Assert.assertTrue(selector.matches(labels));

InternalSelector selector1 = Selector.parse("environment = qa");
Assert.assertFalse(selector1.matches(labels));
``` 

### Verification
There are unit cases which is same as [k8s-selector-test](https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/apimachinery/pkg/labels/selector_test.go) and all passed.



