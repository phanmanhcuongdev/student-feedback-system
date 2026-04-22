IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [content_translated] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [source_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_SurveyQuestion_SourceLang_V9] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey_Question'
         AND c.name = 'source_lang'
   )
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD CONSTRAINT [DF_SurveyQuestion_SourceLang_V9] DEFAULT ('en') FOR [source_lang];
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_SurveyQuestion_IsAutoTranslated_V9] DEFAULT ((0));
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey_Question'
         AND c.name = 'is_auto_translated'
   )
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD CONSTRAINT [DF_SurveyQuestion_IsAutoTranslated_V9] DEFAULT ((0)) FOR [is_auto_translated];
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'entity_id') IS NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'question_id') IS NOT NULL
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
   AND COL_LENGTH(N'dbo.Survey_Question', N'entity_id') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'entity_type') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Translation_Update_V9'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Translation_Update_V9]
    ON [dbo].[Survey_Question] ([entity_type], [entity_id], [is_auto_translated], [source_lang])
    INCLUDE ([content_translated]);
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_translated') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.check_constraints
       WHERE [name] = 'CHK_SurveyQuestion_Translation_Update_Integrity_V9'
         AND [parent_object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    ALTER TABLE [dbo].[Survey_Question] WITH NOCHECK
    ADD CONSTRAINT [CHK_SurveyQuestion_Translation_Update_Integrity_V9]
    CHECK (
        [is_auto_translated] = 0
        OR ([content_translated] IS NOT NULL AND [source_lang] IS NOT NULL)
    );
END
GO
