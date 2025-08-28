package org.bergman.agentic.demo;

import com.embabel.agent.api.common.AiBuilder;
import com.embabel.chat.AssistantMessage;
import com.embabel.chat.Conversation;
import com.embabel.common.ai.model.LlmOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the tools for our agent, tools that all delegate directly to Cypher and Neo4j
 */
@Service
public class SOChatAgentEmbabel {
    private final Neo4jConnection neo4j;
    private final AiBuilder aiBuilder;

    protected SOChatAgentEmbabel(Neo4jConnection neo4j, AiBuilder aiBuilder) {
        this.neo4j = neo4j;
        this.aiBuilder = aiBuilder;
    }

    @Tool(description =
            """
                    Find relevant questions (topics) in the graph based on vector search on the question the user asked (the prompt)
                    """)
    public List<Map<String, Object>> findRelevantQuestions(
            @ToolParam(description = "The question as asked by the user") String userQuestion
    ) throws Exception {
        return neo4j.getRelevantQuestions(userQuestion);
    }

    @Tool(description =
            """
                    For a specific question (topic), get all posts in that thread (the question itself and all answers).
                    The result is unsorted, but there is a created field with when it was posted.
                    """)
    public List<Map<String, Object>> retrieveThread(
            @ToolParam(description = "The id of the question/topic to get the thread for") String questionId
    ) throws Exception {
        return neo4j.getThread(questionId);
    }

    @Tool(description =
            """
                    For a specific question (topic), get the answer that has been indicated as the accepted answer
                    (if there is one, otherwise it returns a string that says 'No accepted answer')
                    """)
    public Map<String, Object> retrieveAcceptedAnswer(
            @ToolParam(description = "The id of the question/topic to get the accepted answer for") String questionId
    ) throws Exception {
        var result = neo4j.getAcceptedAnswer(questionId);
        if (result == null) {
            return new HashMap<>();
        }
        return result;
    }

    @Tool(description =
            """
                    Fetch all comments for a specific post (question or answer).
                    This may be an empty list if there are no comments.
                    """)
    public List<Map<String, Object>> retrieveComments(
            @ToolParam(description = "The id of the post to get the comments for") String postId
    ) throws Exception {
        return neo4j.getComments(postId);
    }

    @Tool(description =
            """
                    Get the user that posted a question, an answer or a comment.
                    """)
    public Map<String, Object> getUser(
            @ToolParam(description = "The id of the post (question or answer) or comment for which to get the user who posted.") String entityId
    ) throws Exception {
        return neo4j.getUser(entityId);
    }

    @Tool(description =
            """
                    Get all posts (questions and answers) posted by a specific user.
                    """)
    public List<Map<String, Object>> getUserPosts(
            @ToolParam(description = "The user id to get the posted posts for.") String userId
    ) throws Exception {
        return neo4j.getUserPosts(userId);
    }

    @Tool(description =
            """
                    Get all comments posted by a specific user.
                    """)
    public List<Map<String, Object>> getUserComments(
            @ToolParam(description = "The user id to get the posted comments for.") String userId
    ) throws Exception {
        return neo4j.getUserComments(userId);
    }

    @Tool(description =
            """
                    Get the post that an answer or a comment was posted on.
                    If there is no parent (i.e. the post was a question) it return the string 'No parent'
                    """)
    public Map<String, Object> getParentPost(
            @ToolParam(description = "The id of the post (answer) or comment") String entityId
    ) throws Exception {
        var result = neo4j.getParentPost(entityId);
        if (result == null) {
            return new HashMap<>();
        }
        return result;
    }

    public AssistantMessage respond(Conversation conversation) {
        return aiBuilder
                .withShowPrompts(true)
                .ai()
                // TODO: Not sure the temperature should be 0.8, could just not
                // set it and use the default. Setting to null is the same as default
                .withLlm(LlmOptions.withDefaultLlm().withTemperature(null))
                .withToolObject(this)
                .respond(conversation.getMessages());
    }
}
