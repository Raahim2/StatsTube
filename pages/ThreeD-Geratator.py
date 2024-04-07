import streamlit as st
from utils import Turn_To_3D

st.title("3D GIF Generator")

prompt = st.chat_input("Enter a prompt")

if(prompt):
    gif = Turn_To_3D(prompt)

    st.image(gif)