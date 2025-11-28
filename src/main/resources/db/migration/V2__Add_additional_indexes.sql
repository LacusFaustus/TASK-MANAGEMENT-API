-- Дополнительные индексы для оптимизации
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_task_comments_created_at ON task_comments(created_at);
CREATE INDEX idx_workspace_members_role ON workspace_members(role);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Индекс для полнотекстового поиска
CREATE INDEX idx_tasks_title_description ON tasks USING gin(
    to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, ''))
);

-- Индекс для тегов
CREATE INDEX idx_task_tags_tag ON task_tags(tag);

-- Функция для поиска задач по тексту
CREATE OR REPLACE FUNCTION search_tasks(search_query TEXT, workspace_id BIGINT)
RETURNS TABLE(id BIGINT, title VARCHAR, description TEXT, rank REAL) AS $$
BEGIN
    RETURN QUERY
    SELECT t.id, t.title, t.description,
           ts_rank(to_tsvector('english', coalesce(t.title, '') || ' ' || coalesce(t.description, '')),
                   plainto_tsquery('english', search_query)) as rank
    FROM tasks t
    WHERE t.workspace_id = search_tasks.workspace_id
      AND (to_tsvector('english', coalesce(t.title, '') || ' ' || coalesce(t.description, ''))
           @@ plainto_tsquery('english', search_query))
    ORDER BY rank DESC;
END;
$$ LANGUAGE plpgsql;
