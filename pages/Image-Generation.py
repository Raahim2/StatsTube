import streamlit as st
from utils import Generate_Image

st.set_page_config(
    page_title="Image Generator",
    page_icon="Static/imgen.svg",
)

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