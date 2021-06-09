# k8s-selector-java
This is k8s-label-selector tool based on jdk in java, it provide same function as [k8s-label-selector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/)

## How to use it

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

# Verification
There are some unit case which is same as [k8s-selector-test](https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/apimachinery/pkg/labels/selector_test.go) and all passed.
