# 🧠 spring-confluence-rag-starter

**Your documentation has the answers — finally. Spring Boot + RAG-powered answers**

# What is this?

A Spring Boot starter module that connects to your **Confluence workspace**, ingests pages into a **vector database**, and uses **OpenAI + LangChain4j** to answer questions about your docs. All answers include source citations — so you know exactly where the truth came from.

It’s dead simple:

1. Configure your Confluence access
2. Run ingestion
3. Query your own internal knowledge using natural language

Designed to help developers, teams, and tools get real answers — without digging through a decade of stale pages and broken links.  Point it at your space, ingest the pages, and start asking questions.

# 🔧 Features

✅ Works out-of-the-box with Spring Boot 3.5+  
✅ Uses OpenAI embeddings for vectorizing your knowledge  
✅ Applies OpenAI ChatGPT to answer your questions   
✅ Auto-ingests Confluence pages via the REST API    
✅ Built on **RAG architecture**  
✅ Extracts and stores **page metadata** (title + URL) to **cite the source of every answer**  
✅ Built as a clean, reusable Spring Boot starter - Just add the dependency, and you are getting started!

# 🧪 Example: Talking to "Helios Core"

To demonstrate the capabilities, this project includes a fictional Confluence space called Helios Core — a sci-fi-themed internal wiki for a deep space mining operation.

The pages follow realistic knowledge base structure: tables, metrics, reports, procedures, and protocols — designed to test retrieval, reasoning, and refusal in a high-noise environment.

Here’s a real page ingested into the vector store:


<img src="assets/MiningOpsDashboardCycle338Example.png" width="460">

_(Click image to enlarge)_

Once ingested, you can ask questions like:

> Q: Which rig had the highest yield per hour during Cycle 338?  
> Q: How many radiation incidents occurred in Bay 4-D?  
> Q: Who executed a manual override using Protocol Theta-9?  
> Q: How many miners were promoted to Overseer rank?

Answers are accurate, grounded in your documentation, and include a reference to the original Confluence page.

# ⚙️ Under the Hood

This project implements a full RAG (Retrieval-Augmented Generation) pipeline for internal documentation using:

## Confluence Ingestion
Ingests pages from your configured Confluence space using the official REST API using an API key you created.
Extracts and indexes:

* Page title and content (the text of the page)
* Metadata (title, ID, and URL for source attribution)

## Vectorization
Uses OpenAI Embeddings (by default the `text-embedding-3-small` model) to transform content into vector space.
Similarly, user questions are vectorized to enable similarity search against the vector space of the knowledge.

## Storage
Stores vectors in PostgreSQL using the [`pgvector`](https://github.com/pgvector/pgvector) extension.
This allows cost-effective similarity search and retrieval of relevant documents based on user queries.

By default, a _top K_ value of `5` most relevant documents are retrieved for each question, with a similarity threshold of `0.5`.

## RAG Chain Execution
This project uses LangChain4j to implement the RAG pipeline, which includes:

* Document retrieval via similarity search
* Prompt construction using relevant context
* Answer generation using OpenAI ChatGPT (by default the `gpt-4o-mini` model)
* Citation of Confluence URLs from retrieved sources

## Spring Boot Starter Design
Everything is bootstrapped using Spring Boot autoconfiguration — minimal setup, zero boilerplate.

# 🚀 Getting Started

First, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.spring-confluence-rag</groupId>
    <artifactId>spring-confluence-rag-starter</artifactId>
    <version>0.0.0</version>
</dependency>
```

Then, configure your OpenAI integration and Confluence access in `application.yml`. Most settings have reasonable defaults, but you can customize them as needed. Here is a full example configuration:

```yaml
ai:
  spring:
    confluence:
      rag:
        embedding-model:
          overlap-fraction: 0.25           # How much each chunk overlaps with the previous one
          chunk-size: 512                  # Max token length per document chunk
          model: text-embedding-3-small    # OpenAI model used for embedding vectors
        confluence:
          base-url: http://localhost:8090  # Confluence base URL
          username: martin                 # Username for Confluence API
          api-key: martin                  # API token for authentication
          spaces:
            - HC                          # Space keys to ingest (can list multiple)
        database:
          hostname: localhost
          port: 5432
          database: confluence
          password: confluence
          username: confluence
        chatModel:
          model: gpt-4o-mini               # OpenAI model used for chat responses
          memory-tokens: 8192              # Max memory tokens to retain in conversation context
          meta-data-keys:                  # Which attributes from the Confluence API to include in the context
            - pageTitle
            - pageUrl
          system-prompt: |                 # The prompt that defines the AI Q&A behavior
            You are a helpful assistant that answers questions based on the content of Confluence pages.
            Answer the question based on the provided context clearly and concisely. Format using Markdown as appropriate.
            If you do not know the answer, explain that you do not know. Do not make up an answer or guess.
            Always cite the source of your information at the end of your message using the format:
            **Source**: [Page Title](Page URL)
          temperature: 1.0                # OpenAI temperature (reduce to increase determinism)
        open-ai:
          api-key: sk...                  # Your OpenAI API key
```

Finally, ingest your Confluence space and start asking questions.
Let's try the example ones from before. Not all questions are answerable from the ingested data, and the system will refuse to answer if it doesn't have enough information, as part of the system design.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleRunner implements ApplicationRunner {
    // ConversationalRetrievalChain is the main entry point for RAG (Retrieval-Augmented Generation) tasks.
    // It allows you to ask questions and get answers based on the ingested documents.
    private final ConversationalRetrievalChain ragChain;

    // ConfluenceIngestor is responsible for ingesting documents from Confluence into the RAG system.
    // It handles the retrieval and processing of documents, making them available for querying.
    private final ConfluenceIngestor confluenceIngestor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Asynchronously ingest documents from Confluence into the RAG system, then run the example questions.
        confluenceIngestor.ingest().thenRun(askExampleQuestions());
    }

    private Runnable askExampleQuestions() {
        return () -> {
            askAndAnswer("Which rig had the highest yield per hour during Cycle 338?");
            askAndAnswer("How many radiation incidents occurred in Bay 4-D?");
            askAndAnswer("Who executed a manual override using Protocol Theta-9?");
            askAndAnswer("How many miners were promoted to Overseer rank?");
        };
    }

    private void askAndAnswer(@NonNull String question) {
        final var answer = ragChain.execute(question);
        log.info("Question: {}", question);
        log.info("Answer: {}", answer);
    }
}
```

## The Result

### Which rig had the highest yield per hour during Cycle 338?

> The rig with the highest yield per hour during Cycle 338 was **TR-B3**, with a yield of **3.68 metric tons per hour**.
> 
> **Source**: [Mining Ops Dashboard (Cycle 338)](http://localhost:8090/pages/viewpage.action?pageId=1310721)

### How many radiation incidents occurred in Bay 4-D?

> The provided information does not specify the number of radiation incidents that occurred in Bay 4-D. It only mentions a radiation spike that triggered a bio-lockdown and a threshold breach related to a specific badge during a decompression event. Therefore, I do not know the exact number of incidents in Bay 4-D.
>
> **Source**: [Mining Ops Dashboard (Cycle 338)](http://localhost:8090/pages/viewpage.action?pageId=1310721)

### Who executed a manual override using Protocol Theta-9?

> The manual override using Protocol Theta-9 was executed by **Cmdr. Vellis Han** in rig **TR-C7**.
>
> **Source**: [Mining Ops Dashboard (Cycle 338)](http://localhost:8090/pages/viewpage.action?pageId=1310721)

### How many miners were promoted to Overseer rank?

> The provided information does not specify how many miners were promoted to Overseer rank. Therefore, I do not know the answer.
>
> **Source**: [Mining Ops Dashboard (Cycle 338)](http://localhost:8090/pages/viewpage.action?pageId=1310721)

# 💭 Why?
* Because tribal knowledge doesn't belong in Slack threads.
* Because search bars are not comprehension engines.
* Because your documentation isn’t broken — it’s just silent.

This project gives it a voice — one that knows what it’s talking about.
All signal, no hallucination.

# 📄 License

MIT — run off and build something smart.
