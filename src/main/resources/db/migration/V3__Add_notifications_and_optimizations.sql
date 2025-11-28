-- Таблица уведомлений
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Индексы для уведомлений
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_type ON notifications(type);

-- Функция для очистки старых уведомлений
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS void AS $$
BEGIN
    DELETE FROM notifications
    WHERE is_read = true
    AND created_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- Расширение для полнотекстового поиска (если не установлено)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_tasks_title_trgm ON tasks USING gin (title gin_trgm_ops);
CREATE INDEX idx_tasks_description_trgm ON tasks USING gin (description gin_trgm_ops);

-- Материализованное представление для статистики
CREATE MATERIALIZED VIEW workspace_stats AS
SELECT
    w.id as workspace_id,
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'DONE' THEN t.id END) as completed_tasks,
    COUNT(DISTINCT wm.user_id) as total_members,
    AVG(CASE WHEN t.status = 'DONE' THEN
        EXTRACT(EPOCH FROM (t.updated_at - t.created_at)) / 3600 END) as avg_completion_hours
FROM workspaces w
LEFT JOIN tasks t ON w.id = t.workspace_id
LEFT JOIN workspace_members wm ON w.id = wm.workspace_id
GROUP BY w.id;

CREATE UNIQUE INDEX idx_workspace_stats_id ON workspace_stats(workspace_id);

-- Функция для обновления материализованного представления
CREATE OR REPLACE FUNCTION refresh_workspace_stats()
RETURNS trigger AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY workspace_stats;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
