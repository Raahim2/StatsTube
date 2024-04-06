import streamlit as st
from utils import Classify_Text

st.set_page_config(
    page_title="Text Classification",
    page_icon="Static/imgen.svg",
)

# Use session_state to persist data across reruns
session_state = st.session_state
if 'POSTITVE' not in session_state:
    session_state.POSTITVE = []
if 'NEGATIVE' not in session_state:
    session_state.NEGATIVE = []

st.title("Text Classification")

comment = st.chat_input("Add a comment")

if comment:
    result = Classify_Text(comment)
 
    first = result[0]
    print(first)

    label = first['label']

    score = first['score']

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
