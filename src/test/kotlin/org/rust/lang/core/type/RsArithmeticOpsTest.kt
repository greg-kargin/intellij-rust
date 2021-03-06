package org.rust.lang.core.type

import org.rust.lang.core.psi.ext.ArithmeticOp

class RsArithmeticOpsTest : RsTypificationTestBase() {

    fun `test same type of lhs and rhs`() = testExprWithAllOps { traitName, itemName, sign ->
        """
        #[lang = "$itemName"]
        pub trait $traitName<RHS=Self> {
            type Output;
            fn $itemName(self, rhs: RHS) -> Self::Output;
        }

        struct Foo;

        impl $traitName for Foo {
            type Output = Foo;
            fn $itemName(self, rhs: Foo) -> Foo { unimplemented!() }
        }

        fn foo(lhs: Foo, rhs: Foo) {
            let x = lhs $sign rhs;
            x
          //^ Foo
        }
        """
    }

    fun `test same type of lhs and rhs with 'Self' output`() = testExprWithAllOps { traitName, itemName, sign ->
        """
        #[lang = "$itemName"]
        pub trait $traitName<RHS=Self> {
            type Output;
            fn $itemName(self, rhs: RHS) -> Self::Output;
        }

        struct Foo;

        impl $traitName for Foo {
            type Output = Self;
            fn $itemName(self, rhs: Self) -> Self { unimplemented!() }
        }

        fn foo(lhs: Foo, rhs: Foo) {
            let x = lhs $sign rhs;
            x
          //^ Foo
        }
        """
    }

    fun `test different types of lhs, rhs and output`() = testExprWithAllOps { traitName, itemName, sign ->
        """
        #[lang = "$itemName"]
        pub trait $traitName<RHS=Self> {
            type Output;
            fn $itemName(self, rhs: RHS) -> Self::Output;
        }

        struct Foo;
        struct Bar;

        impl $traitName<i32> for Foo {
            type Output = Bar;
            fn $itemName(self, rhs: i32) -> Bar { unimplemented!() }
        }

        fn foo(lhs: Foo, rhs: i32) {
            let x = lhs $sign rhs;
            x
          //^ Bar
        }
        """
    }

    fun `test multiple impls`() = testExprWithAllOps { traitName, itemName, sign ->
        """
        #[lang = "$itemName"]
        pub trait $traitName<RHS=Self> {
            type Output;
            fn $itemName(self, rhs: RHS) -> Self::Output;
        }

        struct Foo;
        struct Bar;
        struct FooBar;

        impl $traitName<f64> for Foo {
            type Output = Bar;
            fn $itemName(self, rhs: f64) -> Bar { unimplemented!() }
        }

        impl $traitName<i32> for Foo {
            type Output = FooBar;
            fn $itemName(self, rhs: i32) -> FooBar { unimplemented!() }
        }

        fn foo(lhs: Foo, rhs: i32) {
            let x = lhs $sign rhs;
            x
          //^ FooBar
        }
        """
    }

    fun `test generic lhs and output`() = testExprWithAllOps { traitName, itemName, sign ->
        """
        #[lang = "$itemName"]
        pub trait $traitName<RHS=Self> {
            type Output;
            fn $itemName(self, rhs: RHS) -> Self::Output;
        }

        struct Foo<T1>(T1);
        struct Bar;
        struct FooBar<T2>(T2);

        impl<T> $traitName<Bar> for Foo<T> {
            type Output = FooBar<T>;
            fn $itemName(self, rhs: Bar) -> FooBar<T> { unimplemented!() }
        }

        fn foo(lhs: Foo<i32>, rhs: Bar) {
            let x = lhs $sign rhs;
            x
          //^ FooBar<i32>
        }
        """
    }

    private inline fun testExprWithAllOps(codeGenerator: (String, String, String) -> String) {
        for ((traitName, itemName, sign) in ArithmeticOp.values()) {
            testExpr(codeGenerator(traitName, itemName, sign))
        }
    }
}
