import streamlit as st
from utils import Generate_Text

st.set_page_config(
    page_title="ChatBot",
    page_icon="Static/chatbot.svg",
)

st.title("Chatbot")

selected_model = st.selectbox("Select a model", ["Mistral AI", "llama", "Gpt2"])
if(selected_model =="Mistral AI"):
    offline_path = "Models/MistralAI-Chatbot-Model"
    online_path="TheBloke/Mistral-7B-Instruct-v0.2-GGUF"
if(selected_model =="llama"):
    offline_path = "noexist"
    online_path="TheBloke/Llama-2-7B-Chat-GGUF"
if(selected_model =="Gpt2"):
    offline_path = "noexist"
    online_path="openai-community/gpt2"



if "messages" not in st.session_state:
    st.session_state.messages = []

for message in st.session_state.messages:
    with st.chat_message(message["role"]):
        st.markdown(message["content"])


if prompt := st.chat_input("What is up?"):
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    response_placeholder = st.empty()

    with st.spinner("Generating Response..."):
        response = Generate_Text(prompt, response_placeholder  ,offline_path , online_path)

    st.session_state.messages.append({"role": "assistant", "content": response})
