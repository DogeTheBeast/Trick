const terminal = document.getElementById("terminal");

// Simulate terminal input line
function createInputLine() {
  const line = document.createElement("div");
  line.className = "input-line";

  const prompt = document.createElement("span");
  prompt.className = "prompt";
  prompt.textContent = "> ";

  const input = document.createElement("input");
  input.type = "text";
  input.autofocus = true;

  line.appendChild(prompt);
  line.appendChild(input);
  terminal.appendChild(line);
  input.focus();

  input.addEventListener("keydown", async (e) => {
    if (e.key === "Enter") {
      const command = input.value;
      line.innerHTML = `<span class="prompt">&gt; </span>${command}`;

      const output = await runCompilerCommand(command);
      createInputLine();
      terminal.scrollTop = terminal.scrollHeight;
    }
  });
}

function addOutput(text) {
  const output = document.createElement("div");
  output.textContent = text;
  terminal.appendChild(output);
}

function addErrorOutput(text) {
  const output = document.createElement("div");
  output.textContent = text;
  output.style.color = "red"
  terminal.appendChild(output);
}

// Store original console methods
const originalInfo = console.info;
const originalError = console.error;

// Redirected output buffer
let capturedOutput = [];

// Override console.info
console.info = function (...args) {
  capturedOutput.push({ type: "info", message: args.join(" ") });
  originalInfo.apply(console, args); // Optional: keep writing to browser console
};

// Override console.error
console.error = function (...args) {
  capturedOutput.push({ type: "error", message: args.join(" ") });
  originalError.apply(console, args);
};

async function runCompilerCommand(command) {
  capturedOutput = []; // clear buffer

  try {
    // Your compiler's entry point — assumes it does console logging internally
    await run(command);

    capturedOutput.forEach(entry => {
	    console.log(entry)
	    if(entry.type === "error")
		addErrorOutput("[error] " + entry.message)
	    else { 
		addOutput(entry.message)
	    }
    });
  } catch (err) {
    return `[exception] ${err.message}`;
  }
}

// Kick it off
createInputLine();

