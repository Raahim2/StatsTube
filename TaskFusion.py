import streamlit as st
from utils import *

st.set_page_config(
    page_title="MegaBot",
    page_icon="👋",
)



a =st.sidebar.selectbox("Choose any" , ["o1" ,"o2","o3"])



if(a=="o1"):
 

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

if(a=="o2"):
   
    st.title("Image Generation")

    prompt = st.chat_input("Enter a prompt")

    num = st.slider("Number of steps",1,50)


    if(prompt and num):
        with st.spinner("Generating image"):
            with st.chat_message("user"):
                st.markdown(prompt)
            im = Generate_Image(prompt , num)
            with st.chat_message("assistant"):
                st.markdown("Generated Image ")
                st.image(im)

if(a=="o3"):
    session_state = st.session_state
    if 'POSTITVE' not in session_state:
        session_state.POSTITVE = []
    if 'NEGATIVE' not in session_state:
        session_state.NEGATIVE = []

    st.title("Text Classification")

    comment = st.chat_input("Add a comment")

    if comment:
        result = Classify_Text(comment)
    
        label = result[0]['label']
        score = result[0]['score']

        
        if label == "POSITIVE":
            session_state.POSTITVE.append(comment)
        else:
            session_state.NEGATIVE.append(comment)

    c1, c2 = st.columns(2)

    with c1:
        st.header("Positive")
        for i in session_state.POSTITVE:
            st.success(i)

    with c2:
        st.header("Negative")
        for i in session_state.NEGATIVE:
            st.error(i)
