CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id UUID NOT NULL REFERENCES users (id),
    updated_by_user_id UUID NOT NULL REFERENCES users (id)
);

CREATE INDEX idx_products_created_by_user_id ON products (created_by_user_id);
CREATE INDEX idx_products_updated_by_user_id ON products (updated_by_user_id);
