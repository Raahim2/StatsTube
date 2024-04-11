import streamlit as st

my_generator="This code adds a player character 'P' with a black background color to the grid. It also implements movement based on swipe gestures. You can swipe up, down, left, or right to move the player character accordingly on the grid. The player character cannot move through walls ('W') or outside the grid boundaries."
my_llm_stream = my_generator

st.write("Most objects") 
st.write(["st", "is <", 3]) 
st.write_stream(my_generator)
st.write_stream(my_llm_stream)

st.text("Fixed width text")
st.markdown("_Markdown_") 
st.latex(r""" e^{i\pi} + 1 = 0 """)
st.title("My title")
st.header("My header")
st.subheader("My sub")
st.code("for i in range(8): foo()")
