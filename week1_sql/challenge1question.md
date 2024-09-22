1. What is the total amount each customer spent at the restaurant?
``` sql
SELECT 
  s.customer_id,
  SUM(m.price) AS total
FROM
  sales s
JOIN
  menu m ON s.product_id = m.product_id
GROUP BY
  s.customer_id;
```

2. How many days has each customer visited the restaurant?

```sql
SELECT
  customer_id,
  COUNT(DISTINCT order_date) AS visit_days
FROM
  sales
GROUP BY
  customer_id;
```
   
3. What was the first item from the menu purchased by each customer?
``` sql

```
4. What is the most purchased item on the menu and how many times was it purchased by all customers?

``` sql
SELECT 
  m.product_name,
  COUNT(s.product_id) AS  purchase_count 
FROM  
  sales s
JOIN 
  menu m ON s.product_id = m.product_id
GROUP BY
  m.product_name
ORDER BY
  purchase_count DESC
LIMIT 1  
```

5. Which item was the most popular for each customer?
``` sql
SELECT
  s.customer_id,
  s.product_id,
  COUNT(*) AS order_count
FROM
  sales s
GROUP BY
  s.customer_id, s.product_id
ORDER BY
  s.customer_id, order_count DESC;
```

6. Which item was purchased first by the customer after they became a member?
``` sql

```
10. Which item was purchased just before the customer became a member?
11. 10. What is the total items and amount spent for each member before they became a member?
12. If each $1 spent equates to 10 points and sushi has a 2x points multiplier - how many points would each customer have?
13. In the first week after a customer joins the program (including their join date) they earn 2x points on all items, not just sushi - how many points do customer A and B have at the end of January?
