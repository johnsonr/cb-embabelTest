package org.bergman.agentic.demo;

import com.embabel.agent.config.annotation.EnableAgents;
import com.embabel.agent.config.annotation.LoggingThemes;
import com.embabel.chat.InMemoryConversation;
import com.embabel.chat.UserMessage;
import com.embabel.common.util.AnsiBuilder;
import com.embabel.common.util.AnsiColor;
import com.embabel.common.util.AnsiStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
@EnableAgents(loggingTheme = LoggingThemes.STAR_WARS)
public class AgentApp {

    @Autowired
    private SOChatAgentEmbabel agent;

    private final AnsiBuilder assistantMessageStyle = new AnsiBuilder()
            .withStyle(AnsiColor.BRIGHT_YELLOW, AnsiStyle.BOLD);

    private final String systemMessage =
            """
                    You are assisting a development team with questions on their specific development environment.
                    For this you have a graph which is an export from Stack Overflow for teams. It has posts and comments on those posts.
                    The original post is usually a question, and the other posts are answers on that question.
                    IMPORTANT: Use tools to get answers from the graph knowledge base.
                    All posts and comments have a link to the user that posted them.
                    """;

    public static void main(String[] args) {
        SpringApplication.run(AgentApp.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {
            var conversation = InMemoryConversation.withSystemMessage(systemMessage);
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print(assistantMessageStyle.format("Enter your question (or 'exit' to quit): "));
                    var userInput = scanner.nextLine();
                    if (userInput.trim().equalsIgnoreCase("exit")) {
                        break;
                    }
                    conversation = conversation.withMessage(new UserMessage(userInput));
                    var assistantMessage = agent.respond(conversation);
                    conversation = conversation.withMessage(assistantMessage);
                    System.out.println(
                            assistantMessageStyle.format(assistantMessage.getContent())
                    );
                }
            }
        };
    }
}
