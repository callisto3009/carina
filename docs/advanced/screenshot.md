The screenshot creation logic is provided in the [Screenshot](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/Screenshot.java) class.

Taking screenshots is controlled by rules. Rules are divided into four types - successful driver action, unsuccessful driver action, 
explicit full-size and explicit visible (page or element, depends on the method used).

### Successful driver action

Produced after a successful click, entering text in a field, etc.  
Used in the event listener of the driver used by the Carina Framework. It is also logical to use it in custom event listeners of the driver.

The default rule implementation is provided in the [DefaultSuccessfulDriverActionScreenshotRule](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/DefaultSuccessfulDriverActionScreenshotRule.java) class and has the following logic:

1. A screenshot is taken of only the visible part of the site/application.
2. Whether to create screenshots or not depends on the configuration parameter `auto_screenshot`.

A rule of this type must use `SUCCESSFUL_DRIVER_ACTION` as the return value in the `getScreenshotType` method.

Usage example:
```
// taking a screenshot of the visible part of the page
Screenshot.capture(getDriver(), ScreenshotType.SUCCESSFUL_DRIVER_ACTION);
```
If you need to take a screenshot of just the element:
```
...
 @Override
    public void afterClick(WebElement element) {
        // taking a screenshot of an element
        Screenshot.capture(element, ScreenshotType.SUCCESSFUL_DRIVER_ACTION, "Element clicked");
    }
...
```

### Error while executing driver action

Produced after an unsuccessful click, text input, etc.
Used in the event listener of the driver used by the Carina Framework. It is also logical to use it in custom event listeners of the driver.

The default rule implementation is provided in the [DefaultUnSuccessfulDriverActionScreenshotRule](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/DefaultUnSuccessfulDriverActionScreenshotRule.java) class and has the following logic:

1. Screenshot required.
2. Creating a full page/only visible part of a screenshot depends on the `allow_fullsize_screenshot` configuration value.

<b>Important</b>: Taking a screenshot of the entire page might significantly slow down your tests execution.

A rule of this type must use `UNSUCCESSFUL_DRIVER_ACTION` as the return value in the `getScreenshotType` method.

Usage example:
```
// taking a screenshot of the visible part of the page
Screenshot.capture(getDriver(), ScreenshotType.UNSUCCESSFUL_DRIVER_ACTION);
```

### Explicitly creating a full-page screenshot

Designed for those cases when you need to take a screenshot in the test.

The default rule implementation is provided in the [ExplicitFullSizeScreenshotRule](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/ExplicitFullSizeScreenshotRule.java) class and has the following logic:

1. Screenshot required.
2. t's always full page.

A rule of this type must use `EXPLICIT_FULL_SIZE` as the return value in the `getScreenshotType` method.

Usage example:
```
Screenshot.capture(getDriver(), ScreenshotType.EXPLICIT_FULL_SIZE);
```

<b>Important</b>: Taking a screenshot of the entire page might significantly slow down your tests execution.

### Explicit creation of a screenshot of the visible part of the page/application

Designed for those cases when you need to take a screenshot in the test.

The default rule implementation is provided in the [ExplicitVisibleScreenshotRule](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/ExplicitVisibleScreenshotRule.java) class and has the following logic:

1. Screenshot required.
2. It is always only the visible part of the page/application.

A rule of this type must use `EXPLICIT_VISIBLE` as the return value in the `getScreenshotType` method.

## Create your own rule

You can implement and register any custom rules based on your requirements. The rule must implement the [IScreenshotRule](https://github.com/zebrunner/carina/blob/master/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/screenshot/IScreenshotRule.java) interface.

The rule is registered using `addRule` method of the `Screenshot` class. The rule is registered globally, and if already registered
rule of the same type, it will be overwritten.

All default rules have the following properties (which can be changed):

1. The timeout is calculated by dividing the `explicit_timeout` configuration value by a divisor.
If the screenshot is full-page, then the divisor is `2`, otherwise `3`. 
2. `DEBUG` logging mode performs strict rule compliance checks.
3. The name of the screenshot file is generated by the `System.currentTimeMillis()` method.
4. The folder where screenshots are saved is determined by the value of the `ReportContext.getTestDir().getAbsolutePath()` method.
5. Screenshot resizing is determined by the configuration parameters `big_screen_width` and `big_screen_height`.

To turn off all kinds of screenshots, remove all rules:
```
Screenshot.clearRules()
```

###FAQ
**How can I find all places where automatic screenshot capturing is performed?**

You can take a look into the [DriverListener](https://github.com/zebrunner/carina/blob/1c9b50202e9254545600488e13f326eaa564e034/carina-webdriver/src/main/java/com/qaprosoft/carina/core/foundation/webdriver/listener/DriverListener.java#L276) 
and [CarinaListener](https://github.com/zebrunner/carina/blob/1c9b50202e9254545600488e13f326eaa564e034/carina-core/src/main/java/com/qaprosoft/carina/core/foundation/listeners/CarinaListener.java#L958) to find call hierarchy.
