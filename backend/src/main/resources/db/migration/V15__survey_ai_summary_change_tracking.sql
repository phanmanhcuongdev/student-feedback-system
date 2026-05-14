CREATE TABLE [dbo].[Survey_AI_Source_State] (
    [survey_id] INT NOT NULL,
    [current_comment_count] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_CurrentCount] DEFAULT ((0)),
    [summarized_comment_count] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_SummarizedCount] DEFAULT ((0)),
    [pending_comment_count] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_PendingCount] DEFAULT ((0)),
    [pending_score_sum] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_PendingScore] DEFAULT ((0)),
    [max_pending_score] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_MaxPendingScore] DEFAULT ((0)),
    [topic_counts_json] NVARCHAR(MAX) NULL,
    [pending_topic_counts_json] NVARCHAR(MAX) NULL,
    [current_entropy] FLOAT NOT NULL CONSTRAINT [DF_SurveyAISourceState_CurrentEntropy] DEFAULT ((0)),
    [summarized_entropy] FLOAT NOT NULL CONSTRAINT [DF_SurveyAISourceState_SummarizedEntropy] DEFAULT ((0)),
    [source_version] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_SourceVersion] DEFAULT ((0)),
    [summarized_source_version] INT NOT NULL CONSTRAINT [DF_SurveyAISourceState_SummarizedVersion] DEFAULT ((0)),
    [last_changed_at] DATETIME NULL,
    [last_summarized_at] DATETIME NULL,
    CONSTRAINT [PK_Survey_AI_Source_State] PRIMARY KEY ([survey_id]),
    CONSTRAINT [FK_SurveyAISourceState_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id])
);
GO

CREATE TABLE [dbo].[Survey_AI_Pending_Change] (
    [change_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [response_detail_id] INT NOT NULL,
    [question_id] INT NOT NULL,
    [comment_length] INT NOT NULL,
    [topic] NVARCHAR(50) NOT NULL,
    [keyword_score] INT NOT NULL,
    [sentiment_score] INT NOT NULL,
    [suggestion_score] INT NOT NULL,
    [entropy_impact_score] INT NOT NULL,
    [novelty_score] INT NOT NULL,
    [total_score] INT NOT NULL,
    [source_version] INT NOT NULL,
    [processed] BIT NOT NULL CONSTRAINT [DF_SurveyAIPendingChange_Processed] DEFAULT ((0)),
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyAIPendingChange_CreatedAt] DEFAULT (GETDATE()),
    CONSTRAINT [PK_Survey_AI_Pending_Change] PRIMARY KEY ([change_id]),
    CONSTRAINT [FK_SurveyAIPendingChange_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyAIPendingChange_ResponseDetail] FOREIGN KEY ([response_detail_id]) REFERENCES [dbo].[Response_Detail]([id]),
    CONSTRAINT [FK_SurveyAIPendingChange_Question] FOREIGN KEY ([question_id]) REFERENCES [dbo].[Question]([question_id])
);
GO

CREATE UNIQUE INDEX [UX_SurveyAIPendingChange_ResponseDetail]
    ON [dbo].[Survey_AI_Pending_Change] ([response_detail_id]);
GO

CREATE INDEX [IX_SurveyAIPendingChange_SurveyProcessedCreatedAt]
    ON [dbo].[Survey_AI_Pending_Change] ([survey_id], [processed], [created_at]);
GO
