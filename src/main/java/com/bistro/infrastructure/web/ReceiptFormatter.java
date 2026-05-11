package com.bistro.infrastructure.web;

import com.bistro.domain.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ReceiptFormatter {

    public String generateReceipt(Order order) {
        var sep = "------------------------";
        var sb = new StringBuilder();
        sb.append(sep).append("\n");
        sb.append("Table Nr. ").append(order.getTableNumber()).append("\n");
        sb.append(sep).append("\n");

        for (var item : order.getItems()) {
            var lineTotal = item.calculateSubtotal();
            sb.append(item.quantity())
                .append(" x ").append(item.product().getName())
                .append(" @ ").append(formatAmount(item.product().getPrice()))
                .append(" = ").append(formatAmount(lineTotal))
                .append("\n");
        }

        sb.append(sep).append("\n");
        sb.append("Subtotal: ").append(formatAmount(order.getSubtotal())).append("\n");

        for (var discount : order.getDiscounts()) {
            sb.append(discount.description())
                .append(": -").append(formatAmount(discount.amount()))
                .append("\n");
        }

        sb.append("Total: ").append(formatAmount(order.getTotal())).append("\n");
        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        var stripped = amount.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.setScale(1, RoundingMode.UNNECESSARY).toPlainString();
        }
        return stripped.toPlainString();
    }
}
