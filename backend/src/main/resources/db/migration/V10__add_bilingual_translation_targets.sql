IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'content_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [content_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'content_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [content_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
BEGIN
    UPDATE [dbo].[Feedback]
    SET [content_vi] = COALESCE([content_vi], CASE WHEN [source_lang] = 'vi' THEN COALESCE([content_original], [content]) WHEN [target_lang] = 'vi' THEN [content_translated] END),
        [content_en] = COALESCE([content_en], CASE WHEN [source_lang] = 'en' THEN COALESCE([content_original], [content]) WHEN [target_lang] = 'en' THEN [content_translated] END)
    WHERE [content_vi] IS NULL OR [content_en] IS NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'content_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [content_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'content_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [content_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
BEGIN
    UPDATE [dbo].[Question]
    SET [content_vi] = COALESCE([content_vi], CASE WHEN [source_lang] = 'vi' THEN [content] WHEN [target_lang] = 'vi' THEN [content_translated] END),
        [content_en] = COALESCE([content_en], CASE WHEN [source_lang] = 'en' THEN [content] WHEN [target_lang] = 'en' THEN [content_translated] END)
    WHERE [content_vi] IS NULL OR [content_en] IS NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [content_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [content_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
BEGIN
    UPDATE [dbo].[Survey_Question]
    SET [content_vi] = COALESCE([content_vi], CASE WHEN [source_lang] = 'vi' THEN COALESCE([content_original], [content]) WHEN [target_lang] = 'vi' THEN [content_translated] END),
        [content_en] = COALESCE([content_en], CASE WHEN [source_lang] = 'en' THEN COALESCE([content_original], [content]) WHEN [target_lang] = 'en' THEN [content_translated] END)
    WHERE [content_vi] IS NULL OR [content_en] IS NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Feedback_Bilingual_Content'
         AND [object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    CREATE INDEX [IX_Feedback_Bilingual_Content]
    ON [dbo].[Feedback] ([is_auto_translated], [source_lang])
    INCLUDE ([content_vi], [content_en]);
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Question_Bilingual_Content'
         AND [object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    CREATE INDEX [IX_Question_Bilingual_Content]
    ON [dbo].[Question] ([is_auto_translated], [source_lang])
    INCLUDE ([content_vi], [content_en]);
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Bilingual_Content'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Bilingual_Content]
    ON [dbo].[Survey_Question] ([is_auto_translated], [source_lang])
    INCLUDE ([content_vi], [content_en]);
END
GO
