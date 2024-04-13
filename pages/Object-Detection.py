import streamlit as st
import pandas as pd
import numpy as np

my_dataframe = pd.DataFrame({
    'A': [1, 2, 3, 4, 5],
    'B': ['a', 'b', 'c', 'd', 'e']
})

data = pd.DataFrame({
    'X': [10, 20, 30, 40, 50],
    'Y': [100, 200, 300, 400, 500]
})


# Sample DataFrame
data = {
    'Category': ['A', 'B', 'C', 'D'],
    'Value': [25, 30, 15, 20]
}
df = pd.DataFrame(data)

# Displaying a bar chart
st.bar_chart(df)

# Displaying a line chart
st.line_chart(df)

# Displaying an area chart
st.area_chart(df)

# Displaying a scatter chart
scatter_data = pd.DataFrame({
    'x': np.random.randn(100),
    'y': np.random.randn(100)
})
st.scatter_chart(scatter_data)


my_generator="This code adds a player character 'P' with a black background color to the grid. It also implements movement based on swipe gestures. You can swipe up, down, left, or right to move the player character accordingly on the grid. The player character cannot move through walls ('W') or outside the grid boundaries."
my_llm_stream = my_generator

st.write("Most objects") 
st.write(["st", "is <", 3]) 
#st.write_stream(my_generator)
#st.write_stream(my_llm_stream)

st.text("Fixed width text")
st.markdown("_Markdown_") 
st.latex(r""" e^{i\pi} + 1 = 0 """)
st.title("My title")
st.header("My header")
st.subheader("Explain this code")
st.code("""def f(a, b):
    if a == 0:
        return b
    else:
        return f(a - 1, a + b)

print(f(5, 10))
""")

st.dataframe(my_dataframe)
st.table(data.iloc[0:10])
st.json({"javascript": "react.js", "python": "Django"})
st.metric("My metric", 42, 2)
