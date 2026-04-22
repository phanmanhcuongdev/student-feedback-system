IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'entity_id') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [entity_id] AS CONVERT(INT, [feedback_id]) PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'entity_type') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [entity_type] AS CONVERT(VARCHAR(20), 'FEEDBACK') PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Feedback_Translation_Entity'
         AND [object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    CREATE INDEX [IX_Feedback_Translation_Entity]
    ON [dbo].[Feedback] ([entity_type], [entity_id])
    INCLUDE ([content_translated]);
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Feedback_Translated_Content_Available'
         AND [object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    CREATE INDEX [IX_Feedback_Translated_Content_Available]
    ON [dbo].[Feedback] ([entity_type], [entity_id])
    INCLUDE ([content_translated])
    WHERE [content_translated] IS NOT NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'entity_id') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [entity_id] AS CONVERT(INT, [question_id]) PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'entity_type') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [entity_type] AS CONVERT(VARCHAR(20), 'SURVEY_QUESTION') PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Translation_Entity'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Translation_Entity]
    ON [dbo].[Survey_Question] ([entity_type], [entity_id])
    INCLUDE ([content_translated]);
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Translated_Content_Available'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Translated_Content_Available]
    ON [dbo].[Survey_Question] ([entity_type], [entity_id])
    INCLUDE ([content_translated])
    WHERE [content_translated] IS NOT NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'entity_id') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [entity_id] AS CONVERT(INT, [question_id]) PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'entity_type') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [entity_type] AS CONVERT(VARCHAR(20), 'QUESTION') PERSISTED;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Question_Translation_Entity'
         AND [object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    CREATE INDEX [IX_Question_Translation_Entity]
    ON [dbo].[Question] ([entity_type], [entity_id])
    INCLUDE ([content_translated]);
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Question_Translated_Content_Available'
         AND [object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    CREATE INDEX [IX_Question_Translated_Content_Available]
    ON [dbo].[Question] ([entity_type], [entity_id])
    INCLUDE ([content_translated])
    WHERE [content_translated] IS NOT NULL;
END
GO
