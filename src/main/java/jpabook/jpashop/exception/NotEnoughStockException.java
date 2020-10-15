package jpabook.jpashop.exception;

/**
 * 상품 재고 예외
 */

public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message,Throwable cause) {
        super(message,cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

}