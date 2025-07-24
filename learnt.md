## What's a State Machine?

A state machine is like a **flowchart for your code**. Instead of jumping around randomly, your program has **specific states** and **rules for moving between them**.

Think of a **traffic light**:
- **States**: Red, Yellow, Green
- **Rules**: Red → Green → Yellow → Red (never Red → Yellow)
- **Current state** determines what happens next

In parsing, instead of "read everything then figure it out", you track **where you are** in the parsing process.

## The Big Picture - What We're Solving

Imagine you're reading a letter that arrives **one sentence at a time**:

```
Sentence 1: "Dear John,"
Sentence 2: "Content-Type: image/jpeg"  
Sentence 3: "filename="photo.jpg""
Sentence 4: ""  (empty line - end of headers!)
Sentence 5: "JPEG binary data..."
Sentence 6: "...more binary data..."
Sentence 7: "--boundary123--" (end marker!)
```

You need to know: "Am I still reading the address/headers, or am I reading the actual letter content?"

## The State Machine States

```
State 1: READING_HEADERS
- "I'm looking for filename, content-type, etc."
- "When I see empty line (\r\n\r\n), switch to READING_CONTENT"

State 2: READING_CONTENT  
- "I'm collecting the actual file data"
- "When I see --boundary123--, switch to FOUND_BOUNDARY"

State 3: FOUND_BOUNDARY
- "I found the end, stop processing"
```

## Step-by-Step Code Walk-through

### 1. Setup Phase
```java
private ParseState state = ParseState.READING_HEADERS;  // Start in headers mode
private ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();  // Collect headers
private Path tempContentFile;  // File to store content
private FileOutputStream contentOutput;  // Write content here
```

**What's happening**: Set up containers for different types of data.

### 2. Main Processing Loop
```java
byte[] chunk = new byte[8192];  // Read 8KB at a time
while ((bytesRead = inputStream.read(chunk)) != -1) {
    processChunk(chunk, bytesRead);  // Handle this 8KB piece
}
```

**What's happening**: Instead of loading the entire file, read small pieces and process each piece.

### 3. Processing Each Byte
```java
for (int i = 0; i < bytesRead; i++) {
    byte currentByte = chunk[i];
    patternBuffer.add(currentByte);  // Remember last few bytes
    
    switch (state) {  // What do I do with this byte?
        case READING_HEADERS:
            processHeaderByte(currentByte);
            break;
        case READING_CONTENT:
            processContentByte(currentByte);
            break;
    }
}
```

**What's happening**: Look at each byte and decide what to do based on current state.

### 4. Header Processing State
```java
private void processHeaderByte(byte currentByte) {
    headerBuffer.write(currentByte);  // Add to header collection
    
    // Did I just see the pattern "\r\n\r\n"?
    if (patternBuffer.containsPattern("\r\n\r\n".getBytes())) {
        // Yes! Headers are done
        String headerText = headerBuffer.toString();
        parseHeaders(headerText);  // Extract filename, content-type
        
        state = ParseState.READING_CONTENT;  // Switch states!
    }
}
```

**What's happening**:
- Collect header bytes
- Watch for the "end of headers" signal (`\r\n\r\n`)
- When found, extract filename/content-type and switch to content mode

### 5. Content Processing State
```java
private void processContentByte(byte currentByte) {
    // Did I just see "--boundary123--"?
    if (patternBuffer.containsPattern(endBoundaryPattern)) {
        state = ParseState.DONE;  // Switch states!
        return;
    }
    
    // No boundary yet, this byte is part of the file content
    contentOutput.write(currentByte);  // Save to temp file
}
```

**What's happening**:
- Watch for the "end of content" signal (boundary)
- If no boundary yet, save this byte as part of the file
- When boundary found, stop processing

## The Pattern Buffer Trick

This is the clever part for handling **patterns that span chunks**:

```java
Chunk 1: "...content--boun"
Chunk 2: "dary123--more..."
```

The pattern `--boundary123--` is split across two chunks!

```java
private CircularBuffer patternBuffer;

// For each byte:
patternBuffer.add(currentByte);  // Remember this byte
if (patternBuffer.containsPattern(boundary)) {
    // Found the pattern!
}
```

**What's happening**: The circular buffer remembers the **last N bytes** across chunk boundaries, so you can detect patterns that span chunks.

## Why This Works Better Than Your Original

**Your original approach**:
```
Step 1: Read chunks until headers found
Step 2: ??? (stream position is wrong)
Step 3: Read chunks for content
```

**State machine approach**:
```
Read byte → Check state → Process byte → Maybe switch state → Repeat
```

**Key difference**: One continuous pass through the data, no "backing up" or repositioning needed.

## Real-World Analogy

Imagine you're **sorting mail** as it arrives:

**Bad approach**:
1. Pile up all mail for a week
2. Read all addresses to find which are letters vs packages
3. Process all letters
4. Process all packages

**State machine approach**:
1. Look at each piece of mail as it arrives
2. "Am I reading the address or the content?"
3. Based on current state, put it in the right pile
4. When I see "end of address", switch to "reading content" mode

## When You Reimplement

Focus on these key concepts:

1. **State tracking**: Always know "where am I in the parsing?"
2. **One pass**: Process each byte exactly once, in order
3. **Pattern detection**: Watch for specific byte sequences that mean "switch states"
4. **Buffer management**: Handle patterns that span chunk boundaries
5. **Resource cleanup**: Always close files and free memory

The state machine makes complex parsing **predictable** - you always know what the code is doing and what comes next.