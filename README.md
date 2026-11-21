# Jumia Egypt E-Commerce Test Automation Framework (Autokit)

Comprehensive Test Automation Framework built for **Jumia Egypt** using Java 21, Selenium WebDriver, TestNG, Allure Reporting, and Apache Maven.

---

##  Task & Budget Constraint Requirements

- **Target Budget**: `13,000 EGP`
- **Maximum Products Allowed in Cart**: `4 Products`
- **Tolerance Condition**: If the exact 13,000 EGP cannot be utilized, at least **95%** of the amount must be spent:
  $$\text{Minimum Allowed Cart Total} = 13,000 - (13,000 \times 5\%) = 12,350 \text{ EGP}$$
  $$\mathbf{12,350 \text{ EGP} \le \text{Cart Total} \le 13,000 \text{ EGP}}$$
- Dedicated Test Class: `tests.BudgetCartConstraintTest`

---

##  Design Patterns Applied

### 1. Page Object Model (POM)
Encapsulates each web page's locators, interactions, and assertions:
- [`BasePage.java`](file:///src/main/java/pages/BasePage.java): Core Selenium interactions, explicit waits, JavaScript fallbacks, newsletter popup dismissals, and cookie banner handling.
- [`HomePage.java`](file:///src/main/java/pages/HomePage.java): Jumia landing page, global search bar, cart badge, department navigation.
- [`SearchResultsPage.java`](file:///src/main/java/pages/SearchResultsPage.java): Catalog listing, Brand filter, Price min/max filter, Official Store filter, Customer Rating filter, Sorting (Highest Rated, price, newest), product cards, and pagination.
- [`ProductDetailsPage.java`](file:///src/main/java/pages/ProductDetailsPage.java): Product details (Title, Price, Seller, Rating), Add to Cart button, Add to Wishlist, Recommendations.
- [`CartPage.java`](file:///src/main/java/pages/CartPage.java): Cart items, quantity increments/decrements (1231), item removal, subtotal & total recalculations, recommendations, persistence after refresh.
- [`CheckoutLoginPage.java`](file:///src/main/java/pages/CheckoutLoginPage.java): Guest user checkout login/signup prompt verification.
- [`WishlistModalPage.java`](file:///src/main/java/pages/WishlistModalPage.java): Guest user authentication dialog on clicking wishlist.
- [`ProductItem.java`](file:///src/main/java/pages/models/ProductItem.java) & [`CartItem.java`](file:///src/main/java/pages/models/CartItem.java): Entity models.

### 2. Singleton Design Pattern
- **[`DriverManager.java`](file:///src/test/java/driverFactory/DriverManager.java)**:
  - Thread-Safe Double-Checked Locking Singleton pattern.
  - Manages `WebDriver` instances with `ThreadLocal` for parallel execution support.
  - Controls browser configuration: Chrome (Incognito, headless option, custom arguments), Firefox, and Edge.
- **[`ConfigManager.java`](file:///src/test/java/utils/ConfigManager.java)**:
  - Thread-Safe Singleton pattern.
  - Provides centralized configuration values from `config.properties` and JVM system properties (Target budget, tolerance, timeouts, base URL).

---

##  Data-Driven Testing (DDT) Coverage

The framework implements DDT across all required formats:
1. **Properties**: [`config.properties`](file:///src/main/resources/config.properties) (`baseUrl`, `targetBudget=13000.0`, `budgetTolerance=0.05`, `maxProducts=4`)
2. **JSON**: [`file.json`](file:///src/main/resources/file.json) (Search categories, filters, price boundaries, products list)
3. **CSV**: [`file.csv`](file:///src/main/resources/file.csv) (Diverse search inputs: Valid, Partial, Numbers, Special characters, Arabic names `سامسونج`, Mixed)
4. **Excel**: [`file.xlsx`](file:///src/main/resources/file.xlsx) (Data sheets for Scenarios 1, 3, and Budget Cart)
5. **TestNG DataProvider**: [`DataProviderTest.java`](file:///src/test/java/dataProviders/DataProviderTest.java) connecting test datasets dynamically.

---

##  Document Test Scenarios

| Scenario | Test Class | Description |
| :--- | :--- | :--- |
| **Budget Constraint** | [`BudgetCartConstraintTest.java`](file:///src/test/java/tests/BudgetCartConstraintTest.java) | Validates purchasing products totaling between 12,350 EGP and 13,000 EGP (max 4 products). |
| **Scenario 1** | [`Scenario1_SearchFilterSortTest.java`](file:///src/test/java/tests/Scenario1_SearchFilterSortTest.java) | Advanced search ("Samsung Mobile"), filters (Samsung, 10,000-30,000 EGP, Official Store, Rating 4+), sort by Highest Rated, capture 5 items, traverse 3 pages. |
| **Scenario 2** | [`Scenario2_ProductComparisonCartTest.java`](file:///src/test/java/tests/Scenario2_ProductComparisonCartTest.java) | Wireless headphones, captures 3 products (Name, Price, Seller, Rating), verifies Cart matches PDP, total calculation & refresh persistence. |
| **Scenario 3** | [`Scenario3_CartQuantityPriceTest.java`](file:///src/test/java/tests/Scenario3_CartQuantityPriceTest.java) | Adds product, records unit price, cycles quantity 1231, verifies subtotal and total recalculations. |
| **Scenario 4** | [`Scenario4_MixedCartOperationsTest.java`](file:///src/test/java/tests/Scenario4_MixedCartOperationsTest.java) | Adds Mobile, Headphones, Power Bank; Mobile qty=2; Removes Headphones; Adds Power Bank recommendation; verifies persistence. |
| **Scenario 5** | [`Scenario5_GuestCheckoutTest.java`](file:///src/test/java/tests/Scenario5_GuestCheckoutTest.java) | Guest checkout up to login/signup prompt; verifies order totals; navigates back to cart and verifies intact contents. |
| **Scenario 6** | [`Scenario6_SearchInputsTest.java`](file:///src/test/java/tests/Scenario6_SearchInputsTest.java) | Searches with valid, partial, numbers, special characters, mixed, and Arabic names (`سامسونج`); verifies URL, search box, and no errors. |
| **Scenario 7** | [`Scenario7_WishlistGuestTest.java`](file:///src/test/java/tests/Scenario7_WishlistGuestTest.java) | Guest clicks Add to Wishlist; verifies authentication prompt appears while product details remain visible. |
| **Scenario 8** | [`Scenario8_PaginationConsistencyTest.java`](file:///src/test/java/tests/Scenario8_PaginationConsistencyTest.java) | Searches "Laptop", stores page 1 item, navigates pages 2 & 3, returns to page 1, verifies ordering and loads details page. |

---

##  How to Run Tests

### 1. Run Complete TestNG Suite (`run.xml`)
```bash
mvn clean test
```

### 2. Run Headless Mode
```bash
mvn clean test -Dheadless=true
```

### 3. Run Specific Test
```bash
mvn test -Dtest=BudgetCartConstraintTest
mvn test -Dtest=Scenario1_SearchFilterSortTest
```

### 4. Generate Allure Report
```bash
allure serve target/allure-results
```

