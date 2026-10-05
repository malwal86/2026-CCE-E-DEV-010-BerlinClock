CREATE TABLE conversion (
    id           BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    time         TIME(0)     NOT NULL,
    converted_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX conversion_converted_at_idx ON conversion (converted_at DESC, id DESC);
