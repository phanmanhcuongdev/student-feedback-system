IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'title_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [title_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'title_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [title_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'description_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [description_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'description_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [description_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [source_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_Survey_SourceLang_V11] DEFAULT ('vi');
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'source_lang') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey'
         AND c.name = 'source_lang'
   )
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD CONSTRAINT [DF_Survey_SourceLang_V11] DEFAULT ('vi') FOR [source_lang];
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_Survey_IsAutoTranslated_V11] DEFAULT ((0));
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey'
         AND c.name = 'is_auto_translated'
   )
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD CONSTRAINT [DF_Survey_IsAutoTranslated_V11] DEFAULT ((0)) FOR [is_auto_translated];
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey', N'model_info') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey]
    ADD [model_info] VARCHAR(100) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
BEGIN
    UPDATE [dbo].[Survey]
    SET [source_lang] = COALESCE(NULLIF([source_lang], ''), 'vi'),
        [title_vi] = COALESCE([title_vi], CASE WHEN COALESCE(NULLIF([source_lang], ''), 'vi') = 'vi' THEN [title] END),
        [title_en] = COALESCE([title_en], CASE WHEN COALESCE(NULLIF([source_lang], ''), 'vi') = 'en' THEN [title] END),
        [description_vi] = COALESCE([description_vi], CASE WHEN COALESCE(NULLIF([source_lang], ''), 'vi') = 'vi' THEN [description] END),
        [description_en] = COALESCE([description_en], CASE WHEN COALESCE(NULLIF([source_lang], ''), 'vi') = 'en' THEN [description] END)
    WHERE [source_lang] IS NULL
       OR [title_vi] IS NULL
       OR [title_en] IS NULL
       OR [description_vi] IS NULL
       OR [description_en] IS NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Survey_Bilingual_Content_V11'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey]')
   )
BEGIN
    CREATE INDEX [IX_Survey_Bilingual_Content_V11]
    ON [dbo].[Survey] ([is_auto_translated], [source_lang])
    INCLUDE ([title_vi], [title_en], [description_vi], [description_en]);
END
GO
